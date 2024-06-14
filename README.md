# Cash Desk Module

This project is a Cash Operations Module designed for an internal information system. It enables cashiers to perform cash transactions including deposits and withdrawals in BGN (Bulgarian Lev) and EUR (Euro). The module is optimized for daily operations and ensures accurate and efficient cash management.

## Environment Variable

Set the `API_KEY` environment variable on your machine.


## Usage

Follow these steps to perform cash operations. The server port is 8080.

**Request 1**  
**URL**: http://localhost:8080/api/v1/cash-operation  
**Method**: POST
```java
{
  "type": "WITHDRAWAL" ,
  "currency": "BGN",
  "amount": "100",
  "denomination": {
     "50" : 1,
     "10": 5
  }
}
```

**Request 2**  
**URL**: http://localhost:8080/api/v1/cash-operation  
**Method**: POST
```java
{
  "type": "WITHDRAWAL",
  "currency": "EUR",
  "amount": 500,
  "denomination": {
    "50": 10
  }
}
```

**Request 3**  
**URL**: http://localhost:8080/api/v1/cash-operation  
**Method**: POST
```java
{
  "type": "DEPOSIT",
  "currency": "BGN",
  "amount": 600,
  "denomination": {
     "10" : 10,
     "50" : 10
  }
}
```

**Request 4**  
**URL**: http://localhost:8080/api/v1/cash-operation  
**Method**: POST
```java
{
  "type": "DEPOSIT",
  "currency": "EUR",
  "amount": 200,
  "denomination": {
     "20" : 5,
     "50" : 2
  }
}
```

**Request 5**  
**URL**: localhost:8080/api/v1/cash-balance   
**Method**: GET

