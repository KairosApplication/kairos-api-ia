"""Read-only Atlas inspection. Output contains schema types, never document values."""
import argparse
import json
import os
import re
import socket
import ssl
import sys
from collections import defaultdict
from pathlib import Path
from urllib.parse import urlsplit
from pymongo import MongoClient


def load_credentials(path):
    values = {}
    for line in Path(path).read_text(encoding="utf-8-sig").splitlines():
        match = re.match(r"^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$", line)
        if match:
            values[match[1]] = match[2].strip().strip("\"'")
    for key in ("MONGODB_URI", "MONGODB_DATABASE", "MONGODB_USERNAME", "MONGODB_PASSWORD"):
        if key in os.environ:
            values[key] = os.environ[key]
    uri = values.get("MONGODB_URI", "")
    for key in ("MONGODB_USERNAME", "MONGODB_PASSWORD"):
        uri = uri.replace("${" + key + "}", values.get(key, ""))
    if not uri.startswith(("mongodb://", "mongodb+srv://")) or re.search(r"<[^>]+>|\$\{[^}]+\}", uri):
        raise ValueError("INVALID_URI")
    return uri, values.get("MONGODB_DATABASE", "")


def document_shape(documents):
    types = defaultdict(set)
    occurrences = defaultdict(int)
    count = 0

    def visit(value, path, seen):
        types[path].add(type(value).__name__)
        seen.add(path)
        if isinstance(value, dict):
            for key, item in value.items():
                visit(item, f"{path}.{key}" if path else key, seen)
        elif isinstance(value, list):
            for item in value:
                visit(item, path + "[]", seen)

    for document in documents:
        count += 1
        seen = set()
        for key, value in document.items():
            visit(value, key, seen)
        for path in seen:
            occurrences[path] += 1
    return {"sample_count": count, "fields": {
        key: {"types": sorted(types[key]), "documents_present": occurrences[key],
              "documents_missing": count - occurrences[key]}
        for key in sorted(types)
    }}


def validator_shape(validator):
    """Report validator paths and type/required rules; omit literal field values."""
    rules = []

    def visit(value, path):
        if not isinstance(value, dict):
            return
        item = {"path": path, "keywords": sorted(value)}
        if "bsonType" in value:
            item["bsonType"] = value["bsonType"]
        if "required" in value:
            item["required"] = value["required"]
        rules.append(item)
        for key, nested in value.items():
            if isinstance(nested, dict):
                visit(nested, path + "." + key)
    visit(validator, "validator")
    return rules


def inspect(client, database="", sample_limit=40):
    client.admin.command("ping")
    if not database:
        default = client.get_default_database(default=None)
        if default is not None:
            database = default.name
        else:
            names = [name for name in client.list_database_names()
                     if name not in ("admin", "local", "config")]
            if len(names) != 1:
                return {"status": "DATABASE_SELECTION_REQUIRED", "accessible_databases": names}
            database = names[0]
    db = client[database]
    output = {"status": "OK", "database": database, "collections": []}
    for metadata in db.list_collections():
        name = metadata["name"]
        if name.startswith("system."):
            continue
        collection = db[name]
        # Cursor is bounded; document contents are only inspected in process memory.
        with collection.find({}, limit=sample_limit, max_time_ms=8000) as cursor:
            shape = document_shape(cursor)
        indexes = [{"name": index.get("name"), "fields": list(index["key"].items()),
                    "unique": index.get("unique", index.get("name") == "_id_"), "sparse": index.get("sparse", False),
                    "has_partial_filter": "partialFilterExpression" in index}
                   for index in collection.list_indexes()]
        output["collections"].append({"name": name, **shape, "indexes": indexes,
            "validator_rules": validator_shape(metadata.get("options", {}).get("validator", {}))})
    return output


def diagnose(uri):
    import dns.resolver
    url = urlsplit(uri)
    output = {"status": "DIAGNOSTIC", "database_in_uri": bool(url.path.strip("/")),
              "openssl": ssl.OPENSSL_VERSION}
    if url.scheme == "mongodb+srv":
        records = list(dns.resolver.resolve("_mongodb._tcp." + url.hostname, "SRV", lifetime=8))
        output["srv_records"] = len(records)
        host, port = str(records[0].target).rstrip("."), records[0].port
    else:
        host, port = url.hostname, url.port or 27017
    try:
        with socket.create_connection((host, port), timeout=6) as sock:
            output["tcp"] = "OK"
            # Never disable certificate verification or host name verification.
            with ssl.create_default_context().wrap_socket(sock, server_hostname=host) as tls:
                output["tls"] = tls.version()
    except ssl.SSLError:
        output["tls"] = "HANDSHAKE_FAILED"
    except OSError:
        output["tcp"] = "FAILED"
    return output


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--env-file", default=str(Path.home() / "Downloads/atlas-credentials.env"))
    parser.add_argument("--database", default="")
    parser.add_argument("--sample-limit", type=int, default=40)
    parser.add_argument("--diagnose", action="store_true")
    args = parser.parse_args()
    if not 1 <= args.sample_limit <= 100:
        parser.error("--sample-limit must be between 1 and 100")
    try:
        uri, database = load_credentials(args.env_file)
        if args.diagnose:
            result = diagnose(uri)
        else:
            with MongoClient(uri, serverSelectionTimeoutMS=12000, connectTimeoutMS=5000,
                             socketTimeoutMS=8000) as client:
                result = inspect(client, args.database or database, args.sample_limit)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        return 0 if result["status"] in ("OK", "DIAGNOSTIC") else 3
    except Exception as failure:
        # Driver exception strings/tracebacks may contain secrets. Never print them.
        print(json.dumps({"status": "INSPECTION_FAILED", "error_type": type(failure).__name__,
                          "hint": "Check DNS, TLS, Atlas Network Access, authentication and database selection."}))
        return 2


if __name__ == "__main__":
    sys.exit(main())
