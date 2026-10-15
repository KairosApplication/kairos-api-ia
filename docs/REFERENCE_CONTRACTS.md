# Contratos da API PostgreSQL de referência

Referência: commit 86ea01d. Leitura de controllers e DTOs; nenhum arquivo original alterado.
Todos os endpoints de negócio abaixo estão **pendentes** na nova API: o schema confirmado de kairos_ai pertence ao domínio de IA, sem collections comerciais equivalentes.

| Controller | HTTP | Rota |
| --- | --- | --- |
| AisleController | POST | `/api/v1/aisles/registration` |
| AisleController | GET | `/api/v1/aisles/list` |
| AisleController | GET | `/api/v1/aisles/find/{id}` |
| AisleController | GET | `/api/v1/aisles/find/sector` |
| AisleController | PATCH | `/api/v1/aisles/update/{id}` |
| AisleController | DELETE | `/api/v1/aisles/delete/{id}` |
| AlertController | POST | `/api/v1/alerts/registration` |
| AlertController | GET | `/api/v1/alerts/list` |
| AlertController | GET | `/api/v1/alerts/find/{id}` |
| AlertController | GET | `/api/v1/alerts/find/shelf` |
| AlertController | PATCH | `/api/v1/alerts/update/{id}` |
| AlertController | DELETE | `/api/v1/alerts/delete/{id}` |
| AuthController | GET | `/api/v1/auth/login` |
| AuthController | GET | `/api/v1/auth/me` |
| AuthController | POST | `/api/v1/auth/logout` |
| CategoryController | POST | `/api/v1/categories/registration` |
| CategoryController | GET | `/api/v1/categories/list` |
| CategoryController | GET | `/api/v1/categories/find/{id}` |
| CategoryController | PATCH | `/api/v1/categories/update/{id}` |
| CategoryController | DELETE | `/api/v1/categories/delete/{id}` |
| CustomerController | POST | `/api/v1/customers/registration` |
| CustomerController | GET | `/api/v1/customers/list` |
| CustomerController | GET | `/api/v1/customers/find/{id}` |
| CustomerController | GET | `/api/v1/customers/{id}/recommendations` |
| CustomerController | DELETE | `/api/v1/customers/delete/{id}` |
| EmployeeAisleController | POST | `/api/v1/employee-aisles/registration` |
| EmployeeAisleController | GET | `/api/v1/employee-aisles/list` |
| EmployeeAisleController | GET | `/api/v1/employee-aisles/find/{id}` |
| EmployeeAisleController | GET | `/api/v1/employee-aisles/find/employee` |
| EmployeeAisleController | GET | `/api/v1/employee-aisles/find/aisle` |
| EmployeeAisleController | PATCH | `/api/v1/employee-aisles/update/{id}` |
| EmployeeAisleController | DELETE | `/api/v1/employee-aisles/delete/{id}` |
| EmployeeController | POST | `/api/v1/employees/registration` |
| EmployeeController | GET | `/api/v1/employees/list` |
| EmployeeController | GET | `/api/v1/employees/find/{id}` |
| EmployeeController | PATCH | `/api/v1/employees/update/{id}` |
| EmployeeController | DELETE | `/api/v1/employees/delete/{id}` |
| InventoryController | POST | `/api/v1/inventories/registration` |
| InventoryController | GET | `/api/v1/inventories/list` |
| InventoryController | GET | `/api/v1/inventories/find/{id}` |
| InventoryController | GET | `/api/v1/inventories/find/sector` |
| InventoryController | GET | `/api/v1/inventories/{inventoryId}/products/{productId}/availability` |
| InventoryController | PATCH | `/api/v1/inventories/update/{id}` |
| InventoryController | DELETE | `/api/v1/inventories/delete/{id}` |
| ProductController | POST | `/api/v1/products/registration` |
| ProductController | GET | `/api/v1/products/list` |
| ProductController | GET | `/api/v1/products/list/{brand}` |
| ProductController | GET | `/api/v1/products/find/price` |
| ProductController | GET | `/api/v1/products/find/{id}` |
| ProductController | GET | `/api/v1/products/find/name` |
| ProductController | PATCH | `/api/v1/products/update/{id}` |
| ProductController | DELETE | `/api/v1/products/delete/{id}` |
| ProductShelfController | POST | `/api/v1/product-shelves/registration` |
| ProductShelfController | GET | `/api/v1/product-shelves/list` |
| ProductShelfController | GET | `/api/v1/product-shelves/find/{id}` |
| ProductShelfController | GET | `/api/v1/product-shelves/find/product` |
| ProductShelfController | GET | `/api/v1/product-shelves/find/shelf` |
| ProductShelfController | PATCH | `/api/v1/product-shelves/update/{id}` |
| ProductShelfController | DELETE | `/api/v1/product-shelves/delete/{id}` |
| PurchaseController | GET | `/api/v1/purchases/{id}/total` |
| RestockingController | POST | `/api/v1/restockings/registration` |
| RestockingController | GET | `/api/v1/restockings/list` |
| RestockingController | GET | `/api/v1/restockings/find/{id}` |
| RestockingController | GET | `/api/v1/restockings/find/shelf` |
| RestockingController | GET | `/api/v1/restockings/find/inventory` |
| RestockingController | PATCH | `/api/v1/restockings/update/{id}` |
| RestockingController | DELETE | `/api/v1/restockings/delete/{id}` |
| SectorController | POST | `/api/v1/sectors/registration` |
| SectorController | GET | `/api/v1/sectors/list` |
| SectorController | GET | `/api/v1/sectors/find/{id}` |
| SectorController | GET | `/api/v1/sectors/find/name` |
| SectorController | GET | `/api/v1/sectors/find/type` |
| SectorController | PATCH | `/api/v1/sectors/update/{id}` |
| SectorController | DELETE | `/api/v1/sectors/delete/{id}` |
| ShelfController | POST | `/api/v1/shelves/registration` |
| ShelfController | GET | `/api/v1/shelves/list` |
| ShelfController | GET | `/api/v1/shelves/find/{id}` |
| ShelfController | GET | `/api/v1/shelves/find/aisle` |
| ShelfController | PATCH | `/api/v1/shelves/update/{id}` |
| ShelfController | DELETE | `/api/v1/shelves/delete/{id}` |
| ShelfEmployeeController | POST | `/api/v1/shelf-employees/registration` |
| ShelfEmployeeController | GET | `/api/v1/shelf-employees/list` |
| ShelfEmployeeController | GET | `/api/v1/shelf-employees/find/{id}` |
| ShelfEmployeeController | GET | `/api/v1/shelf-employees/find/employee` |
| ShelfEmployeeController | GET | `/api/v1/shelf-employees/find/shelf` |
| ShelfEmployeeController | PATCH | `/api/v1/shelf-employees/update/{id}` |
| ShelfEmployeeController | DELETE | `/api/v1/shelf-employees/delete/{id}` |
| UserController | POST | `/api/v1/users/registration` |
| UserController | GET | `/api/v1/users/list` |
| UserController | GET | `/api/v1/users/find/{id}` |
| UserController | PATCH | `/api/v1/users/update/{id}` |
| UserController | DELETE | `/api/v1/users/delete/{id}` |

## DTOs de entrada existentes

IDs `Long` abaixo pertencem ao PostgreSQL; o tipo MongoDB ainda não foi confirmado.

### AisleRequest

```java
AisleRequest(
        @NotNull(message = "O setor é obrigatório")
        @Positive(message = "O setor deve ter um ID positivo")
        Long sectorId,
        @NotNull(message = "A posição é obrigatória")
        @Positive(message = "A posição deve ser positiva")
        Integer position
) {
}
```

### AisleUpdateRequest

```java
AisleUpdateRequest(
        @Positive(message = "O setor deve ter um ID positivo") Long sectorId,
        @Positive(message = "A posição deve ser positiva") Integer position
) {
}
```

### AlertRequest

```java
AlertRequest(
        LocalDate stockoutDate,
        @Positive Long employeeId,
        @NotNull @Positive Long shelfId,
        @Positive Long productId,
        @NotBlank @Size(max = 250) String description,
        @NotNull AlertStatus status,
        LocalDateTime resolutionDate
) {
}
```

### AlertUpdateRequest

```java
AlertUpdateRequest(
        LocalDate stockoutDate,
        @Positive Long employeeId,
        @Positive Long shelfId,
        @Positive Long productId,
        @Pattern(regexp = "(?s).*\\S.*") @Size(max = 250) String description,
        AlertStatus status,
        LocalDateTime resolutionDate
) {
}
```

### CategoryRequest

```java
CategoryRequest(
        @NotBlank(message = "Categoria é obrigatória")
        String category
) {
}
```

### CategoryUpdateRequest

```java
CategoryUpdateRequest(
        @NotBlank(message = "Categoria é obrigatória")
        String category
) {
}
```

### CustomerRequest

```java
CustomerRequest(
        @NotNull(message = "O usuário é obrigatório")
        Long userId,
        @NotNull(message = "A empresa é obrigatória")
        @Positive(message = "O ID da empresa deve ser positivo")
        Long companyId
) {
}
```

### EmployeeAisleRequest

```java
EmployeeAisleRequest(
        @NotNull(message = "O funcionário é obrigatório")
        @Positive(message = "O funcionário deve ter um ID positivo") Long employeeId,
        @NotNull(message = "O corredor é obrigatório")
        @Positive(message = "O corredor deve ter um ID positivo") Long aisleId
) {
}
```

### EmployeeAisleUpdateRequest

```java
EmployeeAisleUpdateRequest(
        @Positive(message = "O funcionário deve ter um ID positivo") Long employeeId,
        @Positive(message = "O corredor deve ter um ID positivo") Long aisleId
) {
}
```

### EmployeeRequest

```java
EmployeeRequest(
        @NotNull(message = "O usuário é obrigatório")
        @Valid
        UserRequest user,

        @NotNull(message = "O cargo é obrigatório")
        Position position
) {
}
```

### EmployeeUpdateRequest

```java
EmployeeUpdateRequest(
        @NotNull(message = "O cargo é obrigatório")
        Position position
) {
}
```

### InventoryRequest

```java
InventoryRequest(
        @NotNull(message = "O setor é obrigatório")
        @Positive(message = "O setor deve ter um ID positivo") Long sectorId
) {
}
```

### InventoryUpdateRequest

```java
InventoryUpdateRequest(
        @Positive(message = "O setor deve ter um ID positivo") Long sectorId
) {
}
```

### ProductRequest

```java
ProductRequest (
        @NotNull(message = "A categoria é obrigatória")
        @Positive(message = "A categoria deve ter um ID positivo")
        Long categoryId,
        @NotBlank(message = "O nome da marca é obrigatório")
        @Size(max = 255)
        String brand,
        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.0", message = "O preço não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "O preço deve ter até 10 dígitos inteiros e 2 decimais")
        BigDecimal price,
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100)
        String name
){
}
```

### ProductShelfRequest

```java
ProductShelfRequest(
        @NotNull(message = "O produto é obrigatório")
        @Positive(message = "O produto deve ter um ID positivo") Long productId,
        @NotNull(message = "A prateleira é obrigatória")
        @Positive(message = "A prateleira deve ter um ID positivo") Long shelfId,
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser positiva") Integer productQuantity
) {
}
```

### ProductShelfUpdateRequest

```java
ProductShelfUpdateRequest(
        @Positive(message = "O produto deve ter um ID positivo") Long productId,
        @Positive(message = "A prateleira deve ter um ID positivo") Long shelfId,
        @Positive(message = "A quantidade deve ser positiva") Integer productQuantity
) {
}
```

### ProductUpdateRequest

```java
ProductUpdateRequest(
        @Positive(message = "A categoria deve ter um ID positivo")
        Long categoryId,
        @Pattern(regexp = "(?s).*\\S.*", message = "A marca não pode estar em branco")
        @Size(max = 255)
        String brand,
        @DecimalMin(value = "0.0", message = "O preço não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "O preço deve ter até 10 dígitos inteiros e 2 decimais")
        BigDecimal price,
        @Pattern(regexp = "(?s).*\\S.*", message = "O nome não pode estar em branco")
        @Size(max = 100)
        String name
) {
}
```

### RestockingRequest

```java
RestockingRequest(
        @NotNull @Positive Long inventoryId,
        @NotNull @Positive Long employeeId,
        @NotNull @Positive Long shelfId,
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer restockedQuantity,
        @PositiveOrZero Integer damagedQuantity
) {
}
```

### RestockingUpdateRequest

```java
RestockingUpdateRequest(
        @Positive Long inventoryId,
        @Positive Long employeeId,
        @Positive Long shelfId,
        @Positive Long productId,
        @Positive Integer restockedQuantity,
        @PositiveOrZero Integer damagedQuantity
) {
}
```

### SectorRequest

```java
SectorRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100)
        String name,
        @NotBlank(message = "O tipo é obrigatório")
        @Size(max = 255)
        String type
) {
}
```

### SectorUpdateRequest

```java
SectorUpdateRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "O nome não pode estar em branco")
        @Size(max = 100)
        String name,
        @Pattern(regexp = "(?s).*\\S.*", message = "O tipo não pode estar em branco")
        @Size(max = 255)
        String type
) {
}
```

### ShelfEmployeeRequest

```java
ShelfEmployeeRequest(
        @NotNull(message = "O funcionário é obrigatório")
        @Positive(message = "O funcionário deve ter um ID positivo") Long employeeId,
        @NotNull(message = "A prateleira é obrigatória")
        @Positive(message = "A prateleira deve ter um ID positivo") Long shelfId
) {
}
```

### ShelfEmployeeUpdateRequest

```java
ShelfEmployeeUpdateRequest(
        @Positive(message = "O funcionário deve ter um ID positivo") Long employeeId,
        @Positive(message = "A prateleira deve ter um ID positivo") Long shelfId
) {
}
```

### ShelfRequest

```java
ShelfRequest(
        @NotNull(message = "O corredor é obrigatório")
        @Positive(message = "O corredor deve ter um ID positivo")
        Long aisleId,
        @NotNull(message = "A capacidade máxima é obrigatória")
        @Positive(message = "A capacidade máxima deve ser positiva")
        Integer maximumCapacity
) {
}
```

### ShelfUpdateRequest

```java
ShelfUpdateRequest(
        @Positive(message = "O corredor deve ter um ID positivo") Long aisleId,
        @Positive(message = "A capacidade máxima deve ser positiva") Integer maximumCapacity
) {
}
```

### UserRegistrationRequest

```java
UserRegistrationRequest(
        @NotBlank(message = "O nome é obrigatório") String name,
        @NotBlank(message = "O sobrenome é obrigatório") String lastName,
        @NotNull(message = "A data de nascimento é obrigatória") LocalDate birthDate,
        @NotBlank(message = "A senha é obrigatória") String password,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "O CPF é obrigatório") @CPF(message = "CPF inválido") String cpf,
        @NotNull(message = "A empresa é obrigatória")
        @Positive(message = "O ID da empresa deve ser positivo") Long companyId
) {
}
```

### UserRequest

```java
UserRequest(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O sobrenome é obrigatório")
        String lastName,
        @NotNull(message = "A data de nascimento é obrigatória")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        @NotBlank(message = "A senha é obrigatória")
        String password,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,
        @NotBlank(message = "O CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf
){}
```

### UserUpdateRequest

```java
UserUpdateRequest (
    @Email(message = "E-mail inválido")
    String email,
    @CPF(message = "CPF inválido")
    String cpf
){}
```
