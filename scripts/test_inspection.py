import unittest
from inspect_mongodb import document_shape, validator_shape, inspect
from unittest.mock import MagicMock


class InspectionTest(unittest.TestCase):
    def test_shape_records_optional_null_nested_and_array_types_without_values(self):
        result = document_shape([{"_id": 1, "name": "private-name", "address": {"zip": "private-zip"},
                                 "items": [{"quantity": 2}], "optional": None},
                                {"_id": "different-id-type", "items": []}])
        self.assertEqual(["int", "str"], result["fields"]["_id"]["types"])
        self.assertEqual(1, result["fields"]["name"]["documents_missing"])
        self.assertEqual(["NoneType"], result["fields"]["optional"]["types"])
        self.assertIn("items[].quantity", result["fields"])
        self.assertNotIn("private-name", str(result))
        self.assertNotIn("private-zip", str(result))

    def test_empty_collection_does_not_invent_structure(self):
        self.assertEqual({"sample_count": 0, "fields": {}}, document_shape([]))

    def test_validator_does_not_expose_literals(self):
        output = validator_shape({"$jsonSchema": {"bsonType": "object", "required": ["name"],
                                  "properties": {"name": {"enum": ["private-value"]}}}})
        self.assertNotIn("private-value", str(output))
        self.assertIn("bsonType", str(output))

    def test_inspection_only_uses_read_commands(self):
        client = MagicMock()
        database = client.__getitem__.return_value
        database.list_collections.return_value = [{"name": "observed_collection"}]
        collection = database.__getitem__.return_value
        collection.find.return_value.__enter__.return_value = iter([{"_id": 1}])
        collection.list_indexes.return_value = [{"name": "_id_", "key": {"_id": 1}}]
        result = inspect(client, "verified_db", 3)
        self.assertEqual("OK", result["status"])
        self.assertTrue(result["collections"][0]["indexes"][0]["unique"])
        client.admin.command.assert_called_once_with("ping")
        collection.find.assert_called_once_with({}, limit=3, max_time_ms=8000)
        self.assertEqual({"find", "list_indexes"}, {call[0].split(".")[0].split("(")[0] for call in collection.mock_calls})


if __name__ == "__main__":
    unittest.main()
