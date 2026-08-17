# Flight Booking & Management System
## Master Development Guide — AGENT.md
### Save this file in your project root. Update it as you progress.

**Stack:** Spring Boot · Spring MVC · Spring Security · Spring Data JPA
         Thymeleaf · MySQL · Lombok · Bootstrap 5 · SweetAlert2 · Chart.js
**Tools:** OpenCode (Claude API) · Claude Chat · GPT Free Tier
**Security:** Faculty pattern — CustomAuthenticationProvider → UserDetailsService
              Same-page role rendering via sec:authorize
**Design:** SkyLink Ops design system (UIUX_Requirements.md)

---

## SOLID — Non-Negotiable Rules

| Principle | Concrete Rule for This Project |
|-----------|-------------------------------|
| **S** Single Responsibility | Controller = HTTP only. Service = business logic only. Repository = DB only. Entity = data mapping only. One class, one job. |
| **O** Open/Closed | New feature = new class or new method. Never edit a working method to add new behavior. |
| **L** Liskov Substitution | `FlightServiceImpl` must fully honor every method contract defined in `IFlightService`. No surprise behaviors. |
| **I** Interface Segregation | `IFlightService`, `IBookingService`, `IAircraftService` — separate interfaces. No god-interface with 40 methods. |
| **D** Dependency Inversion | Controllers inject `IFlightService` (interface), NEVER `FlightServiceImpl` (class). Always program to the interface. |

### Package Structure — SOLID Enforced
```
src/main/java/com/yourname/flightapp/
├── config/                      ← WebMvcConfig, app-level config
├── controller/                  ← HTTP layer only — no business logic
├── service/                     ← D: Interfaces only here
│   ├── IFlightService.java
│   ├── IBookingService.java
│   ├── IAircraftService.java
│   ├── ICustomerService.java
│   ├── IUserService.java
│   ├── IDashboardService.java
│   ├── IReportService.java
│   ├── IFileStorageService.java  ← file upload interface
│   └── impl/                    ← D: Implementations here
│       ├── FlightServiceImpl.java
│       ├── BookingServiceImpl.java
│       ├── AircraftServiceImpl.java
│       ├── CustomerServiceImpl.java
│       ├── UserServiceImpl.java
│       ├── DashboardServiceImpl.java
│       ├── ReportServiceImpl.java
│       └── FileStorageServiceImpl.java
├── repository/
├── entity/
├── enums/
├── dto/                         ← Form DTOs, request/response objects
├── record/                      ← Java Records for read-only projections
├── security/
│   ├── SecurityConfiguration.java
│   ├── CustomAuthenticationProvider.java
│   ├── CustomAuthenticationSuccessHandler.java  ← role-based redirect
│   └── CustomUserDetailsService.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── BusinessRuleException.java
│   └── GlobalExceptionHandler.java  ← @ControllerAdvice
└── util/
    └── FileUploadUtil.java
```

---

## Three-Role System

```
ROLE_SUPER_ADMIN
  → Separate dashboard at /superadmin/dashboard
  → Can ONLY: create admins, deactivate admins, view admin list, view audit log
  → Cannot see flights, bookings, customers, reports
  → Created manually in data.sql (seed — cannot be created from UI)
  → Separate nav layout (no flight/booking sidebar items)

ROLE_ADMIN
  → Full system access at /dashboard
  → All CRUD on flights, aircraft, bookings, customers
  → Can create STAFF accounts
  → Can view all reports
  → Cannot access /superadmin/**

ROLE_STAFF  (receptionist)
  → Same /dashboard but limited view
  → Can: search flights, create bookings, view customers, view own bookings
  → Cannot: add/delete flights, manage aircraft, access reports, delete anything
  → Account created by ADMIN, not self-registration
```

### Login Strategy — One Page, Smart Redirect

**Single `/login` page for all three roles.**
After successful login, a `CustomAuthenticationSuccessHandler` reads the user's
role and redirects appropriately:

```java
ROLE_SUPER_ADMIN → /superadmin/dashboard
ROLE_ADMIN       → /dashboard
ROLE_STAFF       → /dashboard
```

This is the real-world pattern — one entry point, role-aware routing.
No separate login URLs needed.

### Self-Registration Rules
```
/signup  → Public — creates ROLE_STAFF account only
           (staff signs up themselves, admin approves or auto-active)

Admin accounts → Created ONLY by ROLE_ADMIN from /admin/users/create
                 Role assigned server-side, never from a form <select>

Super Admin    → Created ONLY via data.sql seed — no UI creation
```

---

## File Upload System

Every entity that has images uses the same upload infrastructure.

### Where files are stored
```
uploads/                          ← outside src/, in project root or configured path
├── aircraft/
├── customers/
├── banners/
└── misc/
```

### IFileStorageService interface
```java
public interface IFileStorageService {
    String store(MultipartFile file, String subfolder) throws IOException;
    Resource loadAsResource(String filename, String subfolder);
    void delete(String filename, String subfolder);
    boolean isValidImageFile(MultipartFile file);
}
```

### Entities that have image upload
| Entity     | Field          | Upload folder   | Display location |
|------------|----------------|-----------------|------------------|
| AppUser    | profilePhoto   | uploads/customers/| Profile card, topbar avatar |
| Aircraft   | aircraftImage  | uploads/aircraft/ | Aircraft list card |
| Customer   | photo          | uploads/customers/| Customer detail card |
| Banner     | imageUrl       | uploads/banners/  | Dashboard/login banner |

### File upload rules
- Accept: jpg, jpeg, png, webp only
- Max size: 2MB per file (configure in application.yml)
- Rename on upload: UUID + original extension (never trust original filename)
- Store path in DB, serve via `/uploads/**` mapped to disk

### application.yml upload config
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 2MB
      max-request-size: 5MB

app:
  upload:
    base-path: uploads/
```

---

## Creative UI Additions (Suggested — Add to Relevant Pages)

These were not in your original spec but significantly improve the presentation:

### Login Page
- **Remember Me** checkbox → Spring Security `.rememberMe()` config, 7-day cookie
- **"I agree to Terms & Conditions"** checkbox → client-side validation before submit
- **Show/Hide password** toggle button → pure JS, eye icon
- **Login error animation** → shake animation on the card when credentials fail (CSS keyframes)
- **Caps Lock warning** → JS detects CapsLock on password field, shows tooltip

### Sign Up Page
- **Password strength meter** → visual bar (Weak/Medium/Strong) as user types
- **Real-time email availability check** → debounced fetch to `/api/check-email`
- **Avatar preview** → if photo upload added, show circular preview before save
- **Terms modal** → "View Terms" opens a SweetAlert2 HTML modal, not a new page

### Flight List & Forms
- **Status filter chips** (not just dropdown) → clickable pill buttons (All / Scheduled / Departed / Cancelled) that filter the table in real time with a small JS filter — no page reload
- **Flight duration auto-calculate** → JS calculates duration when departure + arrival times are set, shows "4h 30m" in a read-only field
- **Seat availability progress bar** → thin colored bar showing % of seats booked (green→amber→red)
- **Route visual** → in flight cards/rows, show "DAC ──✈──→ CGP" instead of plain text

### Booking Pages
- **Multi-step create form** → progress bar at top (Step 1: Flight · Step 2: Customer · Step 3: Confirm), Bootstrap tab/step controls — no separate pages needed
- **Booking summary sidebar** → sticky right panel that updates as the form is filled (JS)
- **Seat class selector** → radio button cards (not a plain <select>): Economy / Business / First Class as styled clickable cards showing price
- **Print booking** → browser `window.print()` with a print-specific CSS that hides nav/sidebar

### Aircraft Pages
- **Image slider** → if aircraft has multiple photos, Bootstrap Carousel
- **Seat map visual** → simple CSS grid showing seat layout (not interactive, just visual)

### Dashboard
- **Live clock** → small JS clock in topbar showing current time (nice touch for receptionist)
- **Greeting** → "Good morning, Admin" based on time of day (JS `Date.getHours()`)
- **Stat card counter animation** → numbers count up from 0 on page load (CSS + small JS)
- **Booking sparkline** → tiny 7-day trend chart inside each stat card using Chart.js

### Customer Pages
- **Avatar initials** → if no photo, generate colored circle with initials (CSS only)
- **Search-as-you-type** → customer list filters in real time as admin types in search box (JS filter on the table, no AJAX needed for small lists)
- **Export to PDF** → browser print button with print CSS (good enough for BSc)

### General UX
- **Breadcrumbs** → under topbar on all inner pages: Dashboard > Flights > Edit Flight
- **Loading states** → disable submit button + show spinner text after form submit
- **Keyboard shortcut hint** → small tooltip on search box showing "Press / to focus search"
- **Empty states** → when a table has no data, show a styled empty state with icon + message + action button (not just blank table)
- **Confirmation of navigation** → if unsaved form, JS `beforeunload` warning

---

## Phase Map

```
Phase 0 ── DONE ✓   Project scaffolding complete
Phase 1 ── Foundation Layer          [Entities · Security · Shell · File Upload]
Phase 2 ── Dashboard Module          [First visual feature — the showcase page]
Phase 3 ── Aircraft Module           [Simplest CRUD — practice the full pattern]
Phase 4 ── Flight Module             [Core domain object]
Phase 5 ── Customer Module           [Prerequisite for bookings]
Phase 6 ── Booking Module            [Most complex — core business feature]
Phase 7 ── Find & Book Flow          [Receptionist primary workflow]
Phase 8 ── Super Admin Panel         [User management, admin creation]
Phase 9 ── Reports Module            [Charts and data export]
Phase 10 ── Polish & Presentation    [Final prep and demo rehearsal]
```

Ask in chat: **"give me Phase [N] detailed guide"** for the full deep-dive.

---

## PHASE 1 — Foundation Layer
**Builds:** Entities · Security (3 roles) · Frontend shell · File upload setup
**Frontend-first:** Shell (sidebar + topbar + base layout) is visible before any data
**Ask:** "give me Phase 1 detailed guide"

### 1A — Entities (Build in FK dependency order)
```
1. Role          ← no FK
2. AppUser       ← FK → Role (ManyToMany)
3. Airport       ← no FK
4. Aircraft      ← no FK (has image field)
5. Flight        ← FK → Aircraft, Airport × 2
6. Customer      ← no FK (has photo field)
7. Booking       ← FK → Flight, Customer, AppUser
8. Banner        ← no FK (image only — for dashboard/login banners)
```
LIMIT via Pageable is the correct JPA pattern — your call is right
findById + delete on IAirportService — yes, Phase 4 needs these for the admin airport management dropdown, good catch
LocalDateTime from/to on flight search — strictly better than LocalDate, keeps the search precise to the hour
### Enums
```java
package enums/
FlightStatus    { SCHEDULED, BOARDING, DEPARTED, ARRIVED, CANCELLED, DELAYED }
BookingStatus   { CONFIRMED, PENDING, CANCELLED, BOARDED, COMPLETED }
AircraftStatus  { ACTIVE, MAINTENANCE, RETIRED }
SeatClass       { ECONOMY, BUSINESS, FIRST_CLASS }
UserStatus      { ACTIVE, INACTIVE, SUSPENDED }
```

### 1B — Security (Faculty Pattern + 3 Roles)
```
security/
├── SecurityConfiguration.java
│     ├── SecurityFilterChain bean
│     ├── BCryptPasswordEncoder bean
│     └── AuthenticationManager bean
├── CustomAuthenticationProvider.java  ← faculty pattern, load from DB
├── CustomUserDetailsService.java      ← implements UserDetailsService
│                                         loads AppUser from DB by email
└── CustomAuthenticationSuccessHandler.java
      ← reads role after login
      ← redirects SUPER_ADMIN → /superadmin/dashboard
      ← redirects ADMIN/STAFF → /dashboard
```

URL rules in SecurityConfiguration:
```java
/login, /signup, /css/**, /js/**, /images/**, /uploads/**  → permitAll()
/superadmin/**           → hasRole("SUPER_ADMIN")
/admin/users/**          → hasRole("ADMIN")
/reports/**              → hasRole("ADMIN")
/flights/add, /flights/delete/**, /flights/edit/**  → hasRole("ADMIN")
/aircraft/add, /aircraft/delete/**, /aircraft/edit/** → hasRole("ADMIN")
/dashboard, /flights, /aircraft, /bookings/**, /customers/** → hasAnyRole("ADMIN","STAFF")
anyRequest()             → authenticated()
```

### 1C — Frontend Shell Files
```
templates/
├── fragments/
│   ├── sidebar.html         ← th:fragment="sidebar"
│   ├── topbar.html          ← th:fragment="topbar(pageTitle)"
│   │                           includes live clock JS + greeting
│   ├── superadmin-sidebar.html ← separate sidebar for super admin layout
│   ├── flash.html           ← SweetAlert2 trigger on flash attributes
│   └── breadcrumb.html      ← th:fragment="breadcrumb(items)"
├── layout/
│   ├── base.html            ← standard layout (admin + staff)
│   └── superadmin-base.html ← super admin layout (different nav)
└── auth/
    ├── login.html           ← Remember Me + Terms + password toggle
    └── signup.html          ← Password strength meter + email check

static/css/
├── main.css                 ← All CSS variables from UIUX_Requirements.md
├── auth.css                 ← Login/signup specific styles
├── sidebar.css
├── forms.css
├── tables.css
└── dashboard.css

static/js/
├── main.js                  ← Live clock, greeting, breadcrumb
├── sweetalert-utils.js      ← All SweetAlert2 wrappers
├── charts.js                ← Chart.js setup
├── form-utils.js            ← Password strength, show/hide, file preview
└── table-filter.js          ← Client-side table search/filter
```

### 1D — File Upload Setup
```
IFileStorageService interface → service/
FileStorageServiceImpl        → service/impl/
FileUploadUtil                → util/
```

Configure `/uploads/**` static resource mapping in `WebMvcConfig.java`:
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
}
```

**Phase 1 Milestone:** Run app → `/login` renders styled → log in as admin → reach `/dashboard` (empty but styled) → sidebar visible with correct nav → topbar shows greeting + clock → log out works.

---

## PHASE 2 — Dashboard Module
**Builds:** Main dashboard page with real data, stat cards, charts, role differences
**Frontend-first:** Static dummy numbers → wire real queries
**Ask:** "give me Phase 2 detailed guide"

### Role differences
```
SUPER_ADMIN → /superadmin/dashboard (different page — user management stats only)

ADMIN dashboard shows:
  KPI cards: Total Flights · Bookings Today · Total Customers · Active Aircraft
  Revenue figures · Recent Bookings table · Upcoming departure dark card
  Bookings this week chart · Staff performance table

STAFF dashboard shows:
  KPI cards: same but NO revenue
  Recent Bookings (own only) · Upcoming departure · Find & Book shortcut
  No chart · No staff table
```

### Java Records for dashboard (in record/ package)
```java
record DashboardStats(long totalFlights, long bookingsToday,
                      long totalCustomers, long activeAircraft,
                      long totalBookings, BigDecimal revenueYtd)
```

### Creative additions on this page
- Stat card counter animation (count up from 0 on load)
- "Good morning/afternoon/evening, [name]" greeting in topbar
- Live clock in topbar corner
- Booking sparkline inside stat cards
- Empty state if no bookings today

---

## PHASE 3 — Aircraft Module
**Builds:** Aircraft CRUD with image upload, status management
**Frontend-first:** list.html with static cards → wire data → add form → edit → delete
**Ask:** "give me Phase 3 detailed guide"

### Service/Impl split (D in SOLID)
```java
// service/IAircraftService.java
public interface IAircraftService {
    List<Aircraft> findAll();
    Aircraft findById(Long id);
    List<Aircraft> findByStatus(AircraftStatus status);
    Aircraft save(AircraftDto dto, MultipartFile image) throws IOException;
    Aircraft update(Long id, AircraftDto dto, MultipartFile image) throws IOException;
    void delete(Long id);
}

// service/impl/AircraftServiceImpl.java
@Service
@RequiredArgsConstructor
public class AircraftServiceImpl implements IAircraftService {
    private final AircraftRepository aircraftRepository;
    private final IFileStorageService fileStorageService;  // D: inject interface
    // implementations...
}
```

### Pages
```
/aircraft          → list — image cards + table, status filter chips
/aircraft/add      → form — with Bootstrap Carousel image preview
/aircraft/edit/{id}→ form — same, prefilled
```

### Creative additions
- Status filter chips (not dropdown) — clickable pills above table
- Aircraft image with fallback placeholder SVG
- Seat count visual (Economy / Business colored bar)
- Bootstrap Carousel if multiple images (future extension hook)
- Image preview before upload (JS FileReader)

---

## PHASE 4 — Flight Module
**Builds:** Flight CRUD, schedule calendar, route visual, seat availability
**Frontend-first:** list with route visuals → form with auto-duration → schedule calendar
**Ask:** "give me Phase 4 detailed guide"

### Service/Impl split
```java
// service/IFlightService.java
public interface IFlightService {
    List<Flight> findAll();
    Flight findById(Long id);
    List<Flight> search(String origin, String dest, LocalDate date, FlightStatus status);
    List<Flight> findUpcomingFlights(int limit);
    Flight save(FlightDto dto);
    Flight update(Long id, FlightDto dto);
    void delete(Long id);
    int getAvailableSeats(Long flightId, SeatClass seatClass);
}

// service/impl/FlightServiceImpl.java
@Service @RequiredArgsConstructor
public class FlightServiceImpl implements IFlightService {
    private final FlightRepository flightRepository;
    private final IAircraftService aircraftService;  // D: interface, not impl
    private final AirportRepository airportRepository;
}
```

### Creative additions
- Route visual: "DAC ──✈──→ CGP" CSS styled display
- Duration auto-calculation JS (departure + arrival → "4h 30m")
- Seat availability progress bar (green→amber→red)
- Status filter chips above table
- Schedule page: CSS grid monthly calendar, click day → show day's flights

---

## PHASE 5 — Customer Module
**Builds:** Customer management with photo upload, booking history
**Frontend-first:** list → form with photo upload → detail with booking history
**Ask:** "give me Phase 5 detailed guide"

### Service/Impl split
```java
// service/ICustomerService.java
public interface ICustomerService {
    List<Customer> findAll();
    Customer findById(Long id);
    List<Customer> search(String query);  // name, email, phone
    Customer save(CustomerDto dto, MultipartFile photo) throws IOException;
    Customer update(Long id, CustomerDto dto, MultipartFile photo) throws IOException;
    void delete(Long id);
    boolean isEmailUnique(String email, Long excludeId);
}
```

### Creative additions
- Initials avatar (CSS only — if no photo)
- Photo upload with circular preview
- Search-as-you-type (JS table filter, no page reload)
- Customer detail: booking history table with status badges
- Amount column hidden for STAFF via sec:authorize

---

## PHASE 6 — Booking Module
**Builds:** Full booking management, role-aware views, complex business rules
**Frontend-first:** list with sec:authorize blocks → detail page → edit status
**Ask:** "give me Phase 6 detailed guide"

### Service/Impl split — SOLID most critical here
```java
// service/IBookingService.java
public interface IBookingService {
    List<Booking> findAll();                          // ADMIN
    List<Booking> findByCreatedBy(AppUser user);      // STAFF — own bookings only
    Booking findById(Long id);
    List<Booking> findByCustomer(Long customerId);
    Booking createBooking(BookingDto dto, AppUser createdBy);
    Booking updateStatus(Long id, BookingStatus status);
    void cancelBooking(Long id, AppUser requestedBy); // validates ownership + rules
    void deleteBooking(Long id);                      // ADMIN only — enforced in SecurityConfig
}
```

### Business rules in BookingServiceImpl (not in controller)
```
cancelBooking rules:
  - STAFF can only cancel their OWN bookings
  - Cannot cancel if status = BOARDED or COMPLETED
  - Cannot cancel within 2 hours of departure
  - On cancel: update BookingStatus, restore seat count on flight
  All these rules = BusinessRuleException thrown from service
  Controller catches exception, adds flash error, redirects
```

### Same-page role rendering on booking list
```html
<!-- bookings/list.html -->
<thead>
  <tr>
    <th>Booking ID</th>
    <th>Customer</th>
    <th>Flight</th>
    <th>Route</th>
    <th>Travel Date</th>
    <th>Class</th>
    <th sec:authorize="hasRole('ADMIN')">Amount</th>  <!-- hidden from STAFF -->
    <th>Status</th>
    <th sec:authorize="hasRole('ADMIN')">Created By</th>
    <th>Actions</th>
  </tr>
</thead>
```

### Creative additions
- Seat class selector: radio button cards (Economy/Business/First Class)
  each styled card shows price + icon, not a plain <select>
- Booking status timeline on detail page (visual step progress)
- Print booking: `window.print()` + print.css hides sidebar/nav
- Multi-step create form with Bootstrap progress steps

---

## PHASE 7 — Find & Book Flow (Receptionist Workflow)
**Builds:** Flight search + multi-step booking creation
**Frontend-first:** search form → result cards → create form with steps
**Ask:** "give me Phase 7 detailed guide"

### Pages
```
/bookings/search   → search form + flight result cards below
/bookings/create   → multi-step form (3 steps)
```

### Multi-step form (same page, Bootstrap tabs or JS step control)
```
Step 1 ── Select Flight
          (pre-filled if coming from /bookings/search?flightId=X)
          Shows flight detail card with route, time, price per class

Step 2 ── Select Customer
          Search existing customer by name/email (dropdown search)
          OR "Add New Customer" inline modal (Bootstrap modal form)
          Select seat class → radio button cards with prices
          Passenger count slider (1–9)

Step 3 ── Confirm & Book
          Booking summary card (dark card style from design system)
          Shows: Flight, Customer, Class, Passengers, Total Price
          "Confirm Booking" primary button
          → POST → success → SweetAlert2 success modal with booking ID
```

### Creative additions
- Passenger count: range slider (1–9) with live number display
- Seat class: radio button cards, price updates dynamically
- Booking summary: sticky right panel updates live as form fills
- Flight result cards: seat availability bar, "X seats left" warning if <10

---

## PHASE 8 — Super Admin Panel
**Builds:** Separate admin management area, user creation, audit trail
**Frontend-first:** superadmin layout shell → user list → create admin form
**Ask:** "give me Phase 8 detailed guide"

### Separate layout
```
templates/
└── superadmin/
    ├── dashboard.html   ← uses superadmin-base.html layout
    ├── users/
    │   ├── list.html    ← All admins + staff list
    │   └── form.html    ← Create/edit admin account
    └── audit/
        └── log.html     ← (optional) who did what — simple action log
```

### What super admin can do
```
View all ROLE_ADMIN and ROLE_STAFF accounts
Create new ADMIN account (name, email, temp password, role assignment)
Activate / Deactivate admin accounts (toggle UserStatus)
Reset admin password (generate temp password, show in modal)
View basic audit log (login events, account created by whom)
```

### What super admin CANNOT see
```
No flights, no bookings, no customers, no aircraft
If /dashboard or /flights are accessed: redirect to /superadmin/dashboard
Enforced by SecurityConfiguration URL rules
```

### Creative additions
- Account status toggle switch (Bootstrap toggle, not a button)
- Temp password shown in SweetAlert2 modal after creation (copy button)
- Last login timestamp shown in user list

---

## PHASE 9 — Reports Module
**Builds:** Chart.js visualizations, data export, ADMIN only
**Frontend-first:** page layout with empty chart containers → wire data
**Ask:** "give me Phase 9 detailed guide"

### Service/Impl split
```java
// service/IReportService.java
public interface IReportService {
    List<BookingsByDay> getBookingsByDay(int days);
    List<RevenueByRoute> getRevenueByRoute(int limit);
    List<StatusBreakdown> getStatusBreakdown();
    List<TopRoute> getTopRoutes(int limit);
}
```

### Java Records for report data
```java
record BookingsByDay(String date, long count)
record RevenueByRoute(String route, BigDecimal revenue)
record StatusBreakdown(String status, long count)
record TopRoute(String origin, String dest, long bookings, BigDecimal revenue)
```

### Charts
```
Line chart   → Bookings per day (last 30 days)
Bar chart    → Revenue per route (top 10)
Doughnut     → Booking status breakdown
              (Confirmed / Pending / Cancelled / Completed)
```

### Creative additions
- Date range picker for report filtering (Bootstrap date inputs)
- Chart color matches design system colors (--secondary, status badge colors)
- Print report: browser print with chart canvas preserved
- Summary cards above charts (Total Revenue, Total Bookings in period)

---

## PHASE 10 — Polish & Presentation
**Builds:** Error pages, SweetAlert audit, seed data, responsive check, demo prep
**Ask:** "give me Phase 10 detailed guide"

### Error pages
```
templates/error/
├── 403.html   ← "Access Denied" — centered, no sidebar, styled
└── 404.html   ← "Page Not Found" — centered, no sidebar, styled
```

### Seed data (data.sql) — enough for a convincing demo
```
3  Users:
   superadmin@skylink.com  (ROLE_SUPER_ADMIN)  bcrypt password
   admin@skylink.com       (ROLE_ADMIN)         bcrypt password
   staff@skylink.com       (ROLE_STAFF)          bcrypt password

5  Airports:    DAC (Dhaka) · CGP (Chittagong) · ZYL (Sylhet) · JSR (Jessore) · CXB (Cox's Bazar)
8  Aircraft:    Mix of Boeing 737, Airbus A320, different statuses, with images
15 Flights:     Mix of statuses, past + future dates, covering all routes
20 Customers:   Realistic Bangladeshi names, emails, phone numbers
35 Bookings:    Mix of statuses, across different flights/customers/staff
```

### SweetAlert2 audit checklist
- [ ] Every delete → confirm dialog with item name
- [ ] Every save (add/edit) → success toast top-right
- [ ] Every validation failure → error toast
- [ ] Booking created → success modal with booking ID + "Print" button
- [ ] Login failure → styled error on login page (not SweetAlert — inline)
- [ ] Session timeout → redirect to login with timeout message on login page
- [ ] Zero browser default alert() anywhere

### Responsive check breakpoints
```
1440px  Desktop — primary target
1024px  Laptop — must work
768px   Tablet — sidebar overlays, hamburger menu appears
375px   Mobile — tables scroll, cards stack
```

### Demo flow script (practice 5 times)
```
Scene 1 — Super Admin
  Open /login → log in as superadmin@skylink.com
  Show: different dashboard, different sidebar, user management only
  Create a new ADMIN account → show temp password modal

Scene 2 — Admin full access
  Log in as admin@skylink.com
  Dashboard → stat cards, greeting, chart, recent bookings
  Add a new flight → trigger validation error on purpose → fix → save → toast
  Aircraft → edit an aircraft → change status to Maintenance
  Reports → show all 3 charts

Scene 3 — Staff (receptionist) workflow
  Log in as staff@skylink.com
  Dashboard → same page but limited (no revenue, no charts)
  Find & Book → search DAC → CGP, pick a flight
  Multi-step: select customer → choose Business class (radio card) → confirm
  Booking success modal → shows booking ID
  Try /reports → 403 page appears

Scene 4 — Admin reviews staff booking
  Log in as admin@skylink.com
  Go to /bookings → see the booking staff just created
  Open detail → full admin view (amount, created by staff name, all actions)
  Cancel it → SweetAlert confirm → cancelled → toast
```

---

## Reference: How to Use This File With Your Tools

### With OpenCode
Paste relevant phase section as context before asking for code generation.
Example prompt to OpenCode:
> "Following SOLID principles with interface in service/ and implementation in service/impl/,
> generate IFlightService interface and FlightServiceImpl for this project.
> [paste Phase 4 service section from this file]"

### With Claude Chat
For each phase, say:
> "give me Phase [N] detailed guide"

For debugging:
> "Phase [N] issue: [paste specific class + error message]"

For page design:
> "design the [page name] HTML following the SkyLink Ops design system"

For review before commit:
> "review this [class name] for SOLID violations before I commit"

### With GPT Free Tier
Only for: quick Java syntax questions, Maven dependency versions,
MySQL query syntax. Not for architecture decisions.

---

## Progress Tracker

Update this section as you complete each phase:

```
Phase 0   [✓] Complete
Phase 1   [ ] Foundation Layer
Phase 2   [ ] Dashboard
Phase 3   [ ] Aircraft Module
Phase 4   [ ] Flight Module
Phase 5   [ ] Customer Module
Phase 6   [ ] Booking Module
Phase 7   [ ] Find & Book Flow
Phase 8   [ ] Super Admin Panel
Phase 9   [ ] Reports
Phase 10  [ ] Polish & Presentation
```

---

## Should You Save This as agent.md in Your Project Root?

YES. Absolutely. Here is why and how:

Save as: `AGENT.md` in your project root (same level as `pom.xml`).

This file serves three purposes:
1. Context file for OpenCode — paste sections when prompting
2. Your personal reference — what goes where, SOLID rules, phase checklist
3. Progress tracker — check off as you complete phases

When you start a new chat session with Claude, paste the relevant
phase section from this file as context so Claude knows exactly
where you are and what has been decided.

Do NOT put this inside src/ — it is a project management document, not code.

---
*Design: SkyLink Ops (UIUX_Requirements.md)*
*Security: SPRING_SECURITY_GUIDE.md (faculty pattern)*
*Last updated: Phase 0 complete — ready for Phase 1*
