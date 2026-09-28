# BookMyRoom — College Guest House Room Booking System

A production-ready Spring Boot backend service and staff management UI designed for a college guest house to manage rooms, guests, date-range availability, conflict-free bookings, cancellations, and monthly reporting.

---

## 🌟 Overview & Features

- **Room Management**: Add and retrieve guest house rooms with capacity, room type, and nightly rate.
- **Guest Registry**: Register and manage guest profiles with phone numbers.
- **Date-Range Room Availability**: Check available rooms for any `fromDate` to `toDate` range.
- **Conflict-Free Booking Creation**: Creates bookings ONLY if no overlapping `CONFIRMED` booking exists.
- **Strict Business Rules**:
  - Overlap prevention using standard range intersection logic.
  - Automatic total cost calculation (`numberOfNights * room.nightlyRate`).
  - Rejection of overlapping requests with `HTTP 409 Conflict`.
- **Booking Cancellation**: Soft cancellation changing status to `CANCELLED`, freeing up the dates immediately for future bookings.
- **Monthly Reporting**: Retrieve bookings for any given month/year.
- **Dark Black 3D Parallax UI**: Served from static resources with mouse-tilt 3D cards, instant cost preview, live availability search, and conflict error shake animations.
- **OpenAPI / Swagger Integration**: Interactive API testing available at `/swagger-ui.html`.

---

## 🛠️ Technology Stack

- **Backend Framework**: Java 17+, Spring Boot 3.3.4
- **Persistence Layer**: Spring Data JPA, Hibernate, H2 Database (`jdbc:h2:mem:bookmyroomdb`)
- **Validation**: Jakarta Validation (`@NotNull`, `@NotBlank`, `@Min`, `@Positive`, `@Valid`)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Build Tool**: Apache Maven
- **Frontend**: HTML5, CSS3 (3D Perspective, CSS Grid/Flexbox), JavaScript (ES Modules, Fetch API)

---

## 🏛️ System Architecture

BookMyRoom follows a clean layered architecture:

```
[ Frontend UI / Postman / Swagger ]
                 ↓
      [ REST Controller Layer ]      (HTTP Request handling & DTO mapping)
                 ↓
      [ Service Layer ]              (Core Business Logic & Conflict Validation)
                 ↓
      [ Repository Layer ]           (Spring Data JPA & Custom JPQL Queries)
                 ↓
      [ Relational Database ]        (In-Memory H2 DB)
```

---

## 🗄️ Database Design & Entity Relationships

```
+-------------------+             +-----------------------+             +-------------------+
|       ROOM        |             |        BOOKING        |             |       GUEST       |
+-------------------+             +-----------------------+             +-------------------+
| id (PK, Long)     | 1         * | id (PK, Long)         | *         1 | id (PK, Long)     |
| type (String)     |<------------| room_id (FK, Room)    |------------>| name (String)     |
| capacity (Int)    |             | guest_id (FK, Guest)  |             | phone (String)    |
| nightlyRate (Dec) |             | from_date (LocalDate) |             +-------------------+
+-------------------+             | to_date (LocalDate)   |
                                  | status (Enum)         |
                                  | total_cost (Decimal)  |
                                  +-----------------------+
```

### Booking Status Enum:
- `CONFIRMED`: Active booking blocking date availability.
- `CANCELLED`: Cancelled booking that does NOT block availability.

---

## 🧠 Booking Conflict & Date Overlap Logic

BookMyRoom strictly enforces the **End-Date Checkout Model**:
- `fromDate`: Check-in date (inclusive).
- `toDate`: Check-out date (exclusive - room becomes available for check-in on `toDate`).

### Overlap Condition (JPQL Query):
Two date ranges `[A_from, A_to)` and `[B_from, B_to)` overlap if and only if:
$$\text{A\_from} < \text{B\_to} \quad \text{AND} \quad \text{A\_to} > \text{B\_from}$$

### Overlap Examples (Existing: 10-Oct to 15-Oct):
| Requested Date Range | Result | Explanation |
| :--- | :--- | :--- |
| `09-Oct to 11-Oct` | **REJECTED (409)** | Overlaps Oct 10 |
| `12-Oct to 16-Oct` | **REJECTED (409)** | Overlaps Oct 12–14 |
| `11-Oct to 14-Oct` | **REJECTED (409)** | Fully enclosed inside existing booking |
| `15-Oct to 18-Oct` | **ALLOWED (201)** | Check-in on checkout day (Oct 15) |

---

## 📑 REST API Endpoint Summary

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/rooms` | Create a new room | `201 Created` |
| `GET` | `/api/rooms` | Retrieve all rooms | `200 OK` |
| `GET` | `/api/rooms/{id}` | Retrieve room by ID | `200 OK` / `404` |
| `PUT` | `/api/rooms/{id}` | Update room details | `200 OK` / `404` |
| `DELETE` | `/api/rooms/{id}` | Delete room | `204 No Content` / `404` |
| `GET` | `/api/rooms/available` | Check room availability by date range | `200 OK` / `400` |
| `POST` | `/api/guests` | Register a new guest | `201 Created` |
| `GET` | `/api/guests` | Retrieve all guests | `200 OK` |
| `GET` | `/api/guests/{id}` | Retrieve guest by ID | `200 OK` / `404` |
| `POST` | `/api/bookings` | Create a new room booking | `201 Created` / `409` |
| `GET` | `/api/bookings` | Retrieve all bookings | `200 OK` |
| `GET` | `/api/bookings/{id}` | Retrieve booking by ID | `200 OK` / `404` |
| `PUT/PATCH` | `/api/bookings/{id}/cancel` | Cancel an existing booking | `200 OK` / `404` |
| `GET` | `/api/bookings/month` | Get bookings for specified year & month | `200 OK` |
| `GET` | `/api/dashboard/stats` | Dashboard room & booking metrics | `200 OK` |

---

## 💡 Sample Requests & Responses

### 1. Create Room (`POST /api/rooms`)
**Request Payload**:
```json
{
  "type": "Executive Suite",
  "capacity": 2,
  "nightlyRate": 2500.00
}
```
**Response (`201 Created`)**:
```json
{
  "id": 1,
  "type": "Executive Suite",
  "capacity": 2,
  "nightlyRate": 2500.00
}
```

---

### 2. Create Booking (`POST /api/bookings`)
**Request Payload**:
```json
{
  "roomId": 1,
  "guestId": 1,
  "fromDate": "2026-10-10",
  "toDate": "2026-10-13"
}
```
**Response (`201 Created`)**:
```json
{
  "id": 1,
  "roomId": 1,
  "roomType": "Executive Suite",
  "guestId": 1,
  "guestName": "Prof. Rajesh Kumar",
  "fromDate": "2026-10-10",
  "toDate": "2026-10-13",
  "status": "CONFIRMED",
  "totalCost": 7500.00
}
```

---

### 3. Overlapping Booking Conflict (`POST /api/bookings`)
**Request Payload**:
```json
{
  "roomId": 1,
  "guestId": 2,
  "fromDate": "2026-10-12",
  "toDate": "2026-10-15"
}
```
**Response (`409 Conflict`)**:
```json
{
  "timestamp": "2026-09-28T10:30:00",
  "status": 409,
  "error": "Room Not Available",
  "message": "Room 1 is already booked for the selected date range."
}
```

---

## 🚀 How to Run the Project

1. **Clone or navigate to project directory**:
   ```bash
   cd d:\smart-port-gate-management\GUESTHOUSE
   ```

2. **Build and test project**:
   ```bash
   mvn clean verify
   ```

3. **Start Spring Boot server**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access Applications**:
   - **Staff Management UI**: `http://localhost:8080/`
   - **Swagger OpenAPI Docs**: `http://localhost:8080/swagger-ui.html`
   - **H2 Web Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bookmyroomdb`, User: `sa`, Password: empty)
