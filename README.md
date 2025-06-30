<h1 align="center">
  Test-nauta-logistic-service
  <br>
</h1>
<h4 align="center">This project is objetive resolve the nauta challenge</h4>

## Objetive

We work with companies in the international logistics sector that face a common challenge: key information about their
operations arrives in a disorganized manner and at different times.
Our clients receive data on bookings, containers, purchase orders, and invoices,
but rarely is all the information available from the start. Sometimes only a purchase order is received, other times a
container, and only over time can everything be connected.
The challenge is to design a solution that allows our platform to interpret, organize, and
relate that information progressively, helping our clients have a clear and reliable view of what is happening in their
logistics operation.
You must design a REST API that allows:

1. Receiving related logistics information (containers, bookings, purchase orders, and invoices).
2. Maintaining consistent relationships between entities, avoiding duplicates or cross-referencing data between clients.
3. Querying these entities individually and displaying the related data.

### Ingest of Date

#### POST /api/email

```json
{
  "booking": "BK123",
  "containers": [
    {
      "container": "MEDU1234567"
    }
  ],
  "orders": [
    {
      "purchase": "PO123",
      "invoices": [
        {
          "invoice": "IN123"
        }
      ]
    }
  ]
}
```

## Endpoints

#### Search endpoints

##### GET /api/orders

``Returns all purchase orders recorded by a customer.``

##### GET /api/containers

``Returns all containers registered by a client.``

##### GET /api/orders/{purchaseId}/containers

``Returns all containers associated with a specific purchase order.``

#### GET /api/containers/{containerId}/orders

``Returns all purchase orders associated with a specific container.``

## Architecture diagram
![image](https://github.com/user-attachments/assets/19085195-d6b4-4561-9ec7-7dfff8c70ace)

## Layers

* Models
* Repository
* Usecase
* Infra
* Delivery
* Consumer

## Dependencies

* Vert.x 4.5.16
* Java 17

### External dependencies

* My Sql
* MongoDb

## How To Use

### Running the project

Before running the project please ensure that all the dependencies are installed in your system. Then follow the next:

1. First step, start enviroment

    ```
    make create
    ```

2.Build project

   ```shell
   ./gradlew  clean build
   ```

### Running the tests

In order to run the project tests you need to execute the following command:

## Container Run test

Move into container service and run

```shell
./gradlew  test
```

## Order Run test

Move into order service and run

```shell
./gradlew test
```

## Orchestrator Run test
Move into orchestrator service and run
```shell
./gradlew test
```

## Postman Collection
Import this collection for test endpoint
```json
{
	"info": {
		"_postman_id": "f0e84b6b-479f-4f8d-8b19-27562149d0db",
		"name": "nauta",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
		"_exporter_id": "3310404"
	},
	"item": [
		{
			"name": "orders",
			"item": [
				{
					"name": "health",
					"request": {
						"method": "GET",
						"header": [],
						"url": {
							"raw": "http://localhost:8080/health",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "8080",
							"path": [
								"health"
							]
						}
					},
					"response": []
				},
				{
					"name": "CreateOrder",
					"request": {
						"method": "POST",
						"header": [
							{
								"key": "X-Client-ID",
								"value": "123",
								"type": "text"
							}
						],
						"body": {
							"mode": "raw",
							"raw": "{\n    \"booking\": \"BK123\",\n    \"containers\": [\n        {\n            \"container\": \"MEDU1234567\"\n        }\n    ],\n    \"orders\": [\n        {\n            \"purchase\": \"PO123\",\n            \"invoices\": [\n                {\n                    \"invoice\": \"IN123\"\n                }\n            ]\n        }\n    ]\n}",
							"options": {
								"raw": {
									"language": "json"
								}
							}
						},
						"url": {
							"raw": "http://localhost:8080/v1/api/email",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "8080",
							"path": [
								"v1",
								"api",
								"email"
							]
						}
					},
					"response": []
				},
				{
					"name": "get-containers-by-purchase",
					"request": {
						"method": "GET",
						"header": [
							{
								"key": "X-Client-ID",
								"value": "123",
								"type": "text"
							}
						],
						"url": {
							"raw": "http://localhost:8080/v1/api/orders/::purchaseId/containers",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "8080",
							"path": [
								"v1",
								"api",
								"orders",
								"::purchaseId",
								"containers"
							],
							"variable": [
								{
									"key": ":purchaseId",
									"value": "PO123"
								}
							]
						}
					},
					"response": []
				}
			]
		},
		{
			"name": "container",
			"item": [
				{
					"name": "get-containers",
					"request": {
						"method": "GET",
						"header": [
							{
								"key": "X-Client-ID",
								"value": "123",
								"type": "text"
							}
						],
						"url": {
							"raw": "http://localhost:8080/v1/api/containers/",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "8080",
							"path": [
								"v1",
								"api",
								"containers",
								""
							]
						}
					},
					"response": []
				},
				{
					"name": "get-orders-container",
					"request": {
						"method": "GET",
						"header": [
							{
								"key": "X-Client-ID",
								"value": "123",
								"type": "text"
							}
						],
						"url": {
							"raw": "http://localhost:8080/v1/api/containers/",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "8080",
							"path": [
								"v1",
								"api",
								"containers",
								""
							]
						}
					},
					"response": []
				}
			]
		},
		{
			"name": "New Request",
			"request": {
				"method": "GET",
				"header": []
			},
			"response": []
		}
	]
}
```

