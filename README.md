Responsibilities

Create and manage customer orders

Track order status (e.g., PENDING, COMPLETED, CANCELLED)

Calculate order totals

Coordinate with cart Service for cart item

Communicate with Payment Service for transactions asynchronously (kafka)

🔗 Key Features

- Place new orders

- Retrieve all orders

- Get order by ID

- Update order status

- Cancel orders

- Calculate total price based on products

 API Endpoints
POST   /api/v1/orders         → Create order
GET    /api/v1/orders         → Get all orders
GET    /api/v1/orders/{id}    → Get order by ID
PUT    /api/v1/orders/{id}    → Update order
DELETE /api/v1/orders/{id}    → Cancel order

 Security

Secured via API Gateway

Requires authentication using JWT

Role-based access:

customer → create and view orders

ADMIN → manage and update orders
