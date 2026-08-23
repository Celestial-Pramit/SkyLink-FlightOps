# UI/UX Requirements Document
## Flight Booking & Management System — Admin Panel
### Based on SkyLink Ops Design Language

---

## 1. Design Identity

**System Name:** SkyLink Ops (or your chosen name)
**Role:** Admin-only internal operations panel
**Design Direction:** Material Design 3-inspired, light theme, deep navy/purple brand color
**Reference Source:** SkyLink Ops HTML template (Document 3)

This is a professional back-office tool, NOT a customer-facing booking site.
Every design decision must serve speed, clarity, and information density.

---

## 2. Color System (CSS Variables — Define Once, Use Everywhere)

All colors are taken directly from the SkyLink Ops Tailwind config and must be
implemented as CSS custom properties in your `main.css`.

### Primary Palette
| Token                    | Hex Value | Usage |
|--------------------------|-----------|-------|
| `--primary`              | `#1d0947` | Sidebar background, page headings, primary text |
| `--primary-container`    | `#32215c` | Sidebar hover states (dark mode), card accents |
| `--on-primary`           | `#ffffff` | Text/icons ON the primary color (sidebar text) |
| `--primary-fixed`        | `#e9ddff` | Light tint backgrounds |
| `--primary-fixed-dim`    | `#cfbcff` | Muted text on primary background (sidebar subtitles) |

### Secondary Palette (Interactive / Accent)
| Token                       | Hex Value | Usage |
|-----------------------------|-----------|-------|
| `--secondary`               | `#68519d` | Active nav item, buttons, links, focus rings |
| `--secondary-container`     | `#c5abff` | Badge backgrounds, hover chips |
| `--on-secondary`            | `#ffffff` | Text on secondary buttons |
| `--secondary-fixed`         | `#e9ddff` | Highlighted text on dark cards |
| `--secondary-fixed-dim`     | `#d1bcff` | Price/amount text on dark flight card |
| `--on-secondary-container`  | `#523b86` | Text on secondary container backgrounds |

### Surface / Background Palette
| Token                         | Hex Value | Usage |
|-------------------------------|-----------|-------|
| `--background`                | `#fdf8fe` | Page background |
| `--surface`                   | `#fdf8fe` | Card background |
| `--surface-container`         | `#f2ecf2` | Input field background |
| `--surface-container-low`     | `#f7f2f8` | Table row hover, subtle backgrounds |
| `--surface-container-lowest`  | `#ffffff` | Input backgrounds |
| `--surface-container-high`    | `#ece6ec` | Pressed state, chips |
| `--surface-container-highest` | `#e6e1e7` | Dividers, skeleton loading |
| `--surface-dim`               | `#ded8de` | Disabled surfaces |

### Semantic / Status Colors
| Token               | Hex Value | Usage |
|---------------------|-----------|-------|
| `--error`           | `#ba1a1a` | Error state, destructive actions |
| `--error-container` | `#ffdad6` | Error badge background |
| `--on-error`        | `#ffffff` | Text on error |

### Status Badge Colors (Custom — not in base palette)
| Status      | Background  | Text      |
|-------------|-------------|-----------|
| Confirmed   | `#e6f4ea`   | `#137333` |
| Pending     | `#fef7e0`   | `#b06000` |
| Cancelled   | `#ffdad6`   | `#ba1a1a` |
| Refunded    | `#ffdad6`   | `#93000a` |
| Boarded     | `#e8eaf6`   | `#3949ab` |
| In Flight   | `#e3f2fd`   | `#0277bd` |

### Outline / Border Colors
| Token              | Hex Value | Usage |
|--------------------|-----------|-------|
| `--outline`        | `#7a7580` | Focused input borders |
| `--outline-variant`| `#cac4d0` | Default card borders, table dividers |

---

## 3. Typography System

**Font Family:** Inter (Google Fonts — single import, all weights)
**Import URL:** `https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap`

| Scale Name    | Size | Line Height | Weight | Letter Spacing | Usage |
|---------------|------|-------------|--------|----------------|-------|
| display-lg    | 32px | 40px        | 700    | -0.02em        | Airport codes (LAX, NRT), hero numbers |
| headline-md   | 24px | 32px        | 600    | -0.01em        | Price amounts, section heroes |
| headline-sm   | 20px | 28px        | 600    | —              | Page titles in topbar |
| title-lg      | 18px | 24px        | 600    | —              | Card titles (All Booking, Payment Transactions) |
| body-md       | 16px | 24px        | 400    | —              | General body text, form values |
| body-sm       | 14px | 20px        | 400    | —              | Table cell text, descriptions |
| label-md      | 12px | 16px        | 600    | 0.05em         | Table headers, filter labels, nav items |
| label-sm      | 11px | 14px        | 500    | —              | Badges, sub-labels, timestamps |
| mono-data     | 14px | 20px        | 500    | —              | All currency amounts, numeric data |

**Rule:** Currency and all numeric data always use `mono-data` style for visual alignment in tables.

---

## 4. Spacing System

All spacing is based on a 4px grid.

| Token    | Value | Usage |
|----------|-------|-------|
| xs       | 4px   | Icon padding, tiny gaps |
| sm       | 8px   | Between related elements, badge padding |
| md       | 16px  | Card internal padding (compact), nav item padding |
| lg       | 24px  | Card internal padding (standard), section gaps |
| xl       | 32px  | Page section gaps, major separations |
| 2xl      | 48px  | Hero/header areas |

---

## 5. Border Radius System

| Token   | Value  | Usage |
|---------|--------|-------|
| DEFAULT | 2px    | Sharp corners (tags, chips) |
| lg      | 4px    | Inputs, selects, table rows |
| xl      | 8px    | Cards, panels, nav items |
| full    | 12px   | Pills, avatars, circular buttons |

**Rule:** Cards use `xl` (8px). Buttons use `xl` (8px). Badges use `full` (12px). Inputs use `lg` (4px).

---

## 6. Component Design Specifications

### 6.1 Sidebar
- **Width:** 260px, fixed position, full height
- **Background:** `--primary` (#1d0947) — deep navy
- **Logo area:** Icon + system name + subtitle, separated by 32px bottom margin
- **Top CTA button:** Full-width, `--secondary` background, white text, 8px radius
  - Example: "New Flight Plan", "Add Booking"
- **Nav items:** 12px label, `--primary-fixed-dim` color when inactive
- **Active nav item:** `--secondary` background (#68519d), white text, 4px radius
- **Nested sub-menu:** Indented 32px, left border `--primary-fixed/20`, 11px font
- **Active sub-item:** `--secondary-fixed` color (#e9ddff), 600 weight
- **Bottom section:** Separator line, Settings + Support links
- **No collapse animation needed** for BSc project — static sidebar is fine

### 6.2 Topbar
- **Height:** 64px, sticky top, z-index 40
- **Background:** `--surface` (#fdf8fe)
- **Bottom border:** 1px solid `--outline-variant`
- **Left:** Page title in `headline-sm`, `--primary` color
- **Center/Right:** Search input (264px width) + notification bell + admin profile chip
- **Search input:** Left icon, `--surface-container-lowest` background, focus ring `--secondary`
- **Notification bell:** Dot indicator using `--error` color
- **Profile chip:** Avatar + name + role + chevron, bordered pill, hover state

### 6.3 Stat / KPI Cards (Dashboard)
- **Background:** `--surface`
- **Border:** 1px solid `--outline-variant`
- **Radius:** 8px
- **Hover:** Subtle shadow + border changes to `--secondary`
- **Structure:** Icon (Material Symbol) + label (label-md) + value (headline-sm) + sub-text (label-sm)
- **Icon container:** 40x40px, `--primary-fixed/20` background, `--primary` icon color

### 6.4 Tables
- **Header row:** `label-md`, `--on-surface-variant` color
- **Header bottom border:** 1px solid `--outline-variant`
- **Data rows:** `body-sm`, `--on-surface` color
- **Row bottom border:** 1px solid `--outline-variant`
- **Row hover:** `--surface-container-low` background
- **Currency cells:** `mono-data` style, right-aligned
- **Action buttons (edit/delete):** Icon-only, `--on-surface-variant` color, no border

### 6.5 Status Badges
- **Padding:** 4px 8px
- **Radius:** `full` (12px) — pill shape
- **Font:** `label-sm` (11px, weight 500)
- **Colors:** See Section 2 Status Badge Colors table above
- **Usage:** Always shown as pills, never as plain text

### 6.6 Primary Action Button
- **Background:** `--secondary` (#68519d)
- **Text:** `--on-secondary` (white)
- **Radius:** 8px
- **Padding:** 8px 16px
- **Font:** `label-md`
- **Hover:** `--secondary` at 90% opacity
- **Usage:** Confirm, Save, Add, Book Ticket

### 6.7 Secondary / Ghost Button
- **Background:** transparent
- **Border:** 1px solid `--outline-variant`
- **Text:** `--on-surface`
- **Radius:** 8px
- **Usage:** Cancel, View All, Filter, Export

### 6.8 Destructive Button (Delete)
- **Background:** `--error-container` (#ffdad6)
- **Text:** `--error` (#ba1a1a)
- **Radius:** 8px
- **Usage:** Delete confirmation, Remove

### 6.9 Form Inputs
- **Background:** `--surface-container-lowest` (white)
- **Border:** 1px solid `--outline-variant`
- **Radius:** 4px
- **Font:** `body-sm`
- **Focus border:** `--secondary`, 1px ring `--secondary`
- **Label:** `label-md`, `--on-surface-variant`, displayed above input
- **Error state:** `--error` border + error text below
- **Placeholder:** `--on-surface-variant` color

### 6.10 Dark Feature Card (Flight Details Card)
Used for: Upcoming Flight, Booking Confirmation, Featured Flight
- **Background:** `--primary` (#1d0947)
- **Text:** `--on-primary` (white)
- **Decorative element:** Blurred `--secondary` circle, top-right, 30% opacity
- **Airport code:** `display-lg` (32px, 700 weight)
- **City name:** `label-md`, `--primary-fixed-dim`
- **Dashed route line:** `--primary-fixed-dim` at 50% opacity
- **Price:** `headline-md`, `--secondary-fixed-dim` color
- **Grid details:** 2-column grid (Date/Gate/Departure/Class)

### 6.11 Booking List Item (Card-style row)
- **Border:** 1px solid `--outline-variant`
- **Radius:** 4px
- **Hover:** `--surface-container-low` background
- **Structure:** [Destination icon + name] — [Route line with duration] — [Date + Status badge]
- **Icon container:** 40x40, `--primary-fixed/20` bg, `--primary` icon

### 6.12 SweetAlert2 Configuration
Configure once in `sweetalert-utils.js`, import across all pages.

| Trigger           | Type          | Config |
|-------------------|---------------|--------|
| Delete confirm    | Warning modal | "Are you sure?" + Confirm/Cancel buttons |
| Success (add/edit)| Toast         | Top-right, 3s, green icon |
| Error             | Toast         | Top-right, 4s, red icon |
| Booking confirmed | Success modal | Centered, booking summary |

SweetAlert2 color overrides must match `--secondary` for confirm button.

---

## 7. Page Inventory

These are all the screens that need to be designed, in recommended build order.

### Group 1 — Authentication
| # | Page | Path |
|---|------|------|
| 1 | Login | `/login` |

### Group 2 — Dashboard
| # | Page | Path |
|---|------|------|
| 2 | Dashboard / Overview | `/dashboard` |

### Group 3 — Flight Management
| # | Page | Path |
|---|------|------|
| 3 | Flight List | `/flights` |
| 4 | Add Flight | `/flights/add` |
| 5 | Edit Flight | `/flights/edit/{id}` |
| 6 | Flight Schedule (calendar view) | `/flights/schedule` |

### Group 4 — Aircraft Management
| # | Page | Path |
|---|------|------|
| 7 | Aircraft List | `/aircraft` |
| 8 | Add Aircraft | `/aircraft/add` |
| 9 | Edit Aircraft | `/aircraft/edit/{id}` |

### Group 5 — Booking Management
| # | Page | Path |
|---|------|------|
| 10 | All Bookings | `/bookings` |
| 11 | Booking Detail | `/bookings/{id}` |
| 12 | Find Flights (Receptionist search) | `/bookings/search` |
| 13 | Create Booking (Receptionist) | `/bookings/create` |

### Group 6 — Customer Management
| # | Page | Path |
|---|------|------|
| 14 | Customer List | `/customers` |
| 15 | Customer Detail / Profile | `/customers/{id}` |
| 16 | Add Customer | `/customers/add` |

### Group 7 — Reports
| # | Page | Path |
|---|------|------|
| 17 | Reports Overview | `/reports` |

### Group 8 — Error Pages
| # | Page | Path |
|---|------|------|
| 18 | 403 Forbidden | `/error/403` |
| 19 | 404 Not Found | `/error/404` |

**Total: 19 pages**

---

## 8. Page-by-Page Content Requirements

### Page 1 — Login
**Layout:** Centered card, no sidebar
**Left panel (60%):** Branded background using `--primary`, decorative plane graphic,
system name, tagline
**Right panel (40%):** White card with login form
**Form fields:** Email/Username + Password
**Actions:** Login button (primary), "Forgot password?" link
**Feedback:** SweetAlert2 toast on failed login

### Page 2 — Dashboard
**Layout:** Sidebar + Topbar + content area
**Content sections:**
- Row 1: 4 KPI stat cards — Total Flights, Bookings Today, Total Customers, Active Aircraft
- Row 2 Left (8 cols): Recent Bookings list (booking-item style) + Payment Transactions table
- Row 2 Right (4 cols): Upcoming/Next Departure dark card + 2 mini stat cards
- Optional: 1 Chart.js line chart (Bookings this week)
**Data shown:** Live counts, last 5 bookings, last 3 transactions

### Page 3 — Flight List
**Layout:** Standard page
**Top:** Page title + "Add Flight" primary button
**Filter bar:** Origin dropdown, Destination dropdown, Status dropdown, Date picker, Search input
**Content:** Table with columns — Flight No, Origin, Destination, Departure, Arrival, Aircraft, Status, Actions
**Actions per row:** Edit (icon) + Delete (icon with SweetAlert confirm)
**Status badges:** Scheduled, Departed, Arrived, Cancelled, Delayed

### Page 4 — Add Flight / Page 5 — Edit Flight
**Layout:** Standard page with form card (no sidebar content needed)
**Form sections:**
- Section 1: Flight Information — Flight number, Airline, Status
- Section 2: Route — Origin airport, Destination airport
- Section 3: Schedule — Departure date/time, Arrival date/time, Duration (auto-calculated)
- Section 4: Aircraft — Aircraft dropdown (populated from DB)
- Section 5: Pricing — Economy price, Business price, First class price
**Actions:** Save (primary) + Cancel (ghost) — positioned at form bottom
**Validation:** All fields show inline error messages on blur

### Page 6 — Flight Schedule
**Layout:** Calendar view (CSS grid, no external calendar library needed)
**Content:** Monthly calendar, each day shows flight count badge
**Clicking a day:** Shows flights for that day in a panel below or sidebar
**Top controls:** Month/Year navigation, "Today" button

### Page 7 — Aircraft List
**Table columns:** Aircraft ID, Model, Type Code, Manufacturer, Seat Capacity,
Economy seats, Business seats, Status, Actions
**Status badges:** Active, Maintenance, Retired
**Top:** "Add Aircraft" primary button

### Page 8 — Add Aircraft / Page 9 — Edit Aircraft
**Form fields:** Registration number, Model name, Manufacturer,
Aircraft type code, Total seats, Economy seats, Business seats, Status
**Actions:** Save + Cancel

### Page 10 — All Bookings
**Filter bar:** Customer name/search, Flight number, Status, Date range
**Table columns:** Booking ID, Customer, Flight, Route, Date booked,
Travel date, Class, Amount, Status, Actions
**Actions:** View Detail, Edit, Cancel Booking (with SweetAlert confirm)

### Page 11 — Booking Detail
**Layout:** 2-column — Left: booking info card (dark card style), Right: customer info + payment info
**Left dark card:** Origin → Destination, flight details, date, class, seat
**Right:** Customer name, contact, booking date, payment status, amount
**Actions:** Print/Export, Cancel Booking, Edit

### Page 12 — Find Flights (Receptionist Search)
**Top section:** Large search form — Origin, Destination, Travel Date, Class, Passengers
**Search button:** Prominent primary button
**Results:** Flight result cards (similar to booking-item style) showing
Flight No, Route, Departure time, Arrival time, Duration, Available seats, Price
**Each result card:** "Book for Customer" button

### Page 13 — Create Booking
**Step layout (3 steps shown as progress bar at top):**
- Step 1: Select customer (search existing or add new)
- Step 2: Select class + seats
- Step 3: Confirm + payment info
**Booking summary card:** Right panel, dark card style, updates as admin fills form

### Page 14 — Customer List
**Table columns:** Customer ID, Full Name, Email, Phone, NID/Passport,
Total Bookings, Registered Date, Actions
**Actions:** View, Edit, Delete
**Search:** By name, email, or phone

### Page 15 — Customer Detail
**Layout:** 2-column — Left: profile card, Right: booking history table
**Profile card:** Avatar initials, name, email, phone, NID, registration date
**Booking history:** Table of all bookings by this customer with status badges

### Page 16 — Add Customer
**Form fields:** Full name, Email, Phone, NID or Passport number, Date of birth, Address
**Validation:** Email format, phone format, required fields

### Page 17 — Reports
**Sections:**
- Bookings by month (line chart — Chart.js)
- Revenue by route (bar chart — Chart.js)
- Top 5 routes (table)
- Booking status breakdown (doughnut chart — Chart.js)
- Export button (Print / future CSV)

### Page 18 — 403 / Page 19 — 404
**Layout:** Centered, no sidebar
**Content:** Large icon, error code, message, "Go to Dashboard" button

---

## 9. Thymeleaf Template Structure

```
templates/
├── layout/
│   └── base.html          ← Master layout: sidebar + topbar + content block
├── fragments/
│   ├── sidebar.html       ← Sidebar fragment (th:fragment="sidebar")
│   ├── topbar.html        ← Topbar fragment (th:fragment="topbar")
│   ├── flash-messages.html← SweetAlert2 trigger fragment based on flash attributes
│   └── pagination.html    ← Reusable pagination component
├── auth/
│   └── login.html         ← Standalone layout (no sidebar)
├── dashboard/
│   └── index.html
├── flights/
│   ├── list.html
│   ├── form.html          ← Shared for add + edit (controlled by model flag)
│   └── schedule.html
├── aircraft/
│   ├── list.html
│   └── form.html
├── bookings/
│   ├── list.html
│   ├── detail.html
│   ├── search.html
│   └── create.html
├── customers/
│   ├── list.html
│   ├── detail.html
│   └── form.html
├── reports/
│   └── index.html
└── error/
    ├── 403.html
    └── 404.html
```

---

## 10. Static Resource Structure

```
static/
├── css/
│   ├── main.css           ← CSS variables, base overrides, global styles
│   ├── sidebar.css        ← Sidebar-specific styles
│   ├── forms.css          ← Input, select, label, validation styles
│   ├── tables.css         ← Table, badge, row styles
│   └── dashboard.css      ← Stat cards, chart containers, booking items
├── js/
│   ├── main.js            ← Global init, topbar behavior
│   ├── sweetalert-utils.js← All SweetAlert2 config and wrapper functions
│   ├── charts.js          ← Chart.js setup for dashboard and reports
│   └── booking-form.js    ← Multi-step booking form logic
└── images/
    ├── logo.svg
    └── auth-bg.svg        ← Login page decoration
```

---

## 11. Frontend Libraries (Final Approved List)

| Library         | Version  | How loaded  | Purpose |
|-----------------|----------|-------------|---------|
| Bootstrap 5     | 5.3.x    | CDN         | Grid, modals, collapse, forms |
| Inter (Google)  | —        | Google Fonts| Primary typeface |
| Material Symbols| —        | Google Fonts| Icons throughout |
| SweetAlert2     | 11.x     | CDN         | All alerts, confirms, toasts |
| Chart.js        | 4.x      | CDN         | Dashboard + Reports charts |

**Nothing else.** No jQuery, no AOS, no AdminLTE, no Tailwind runtime in production.

Bootstrap 5 handles: grid layout, sidebar collapse on mobile, modals for forms,
dropdowns in topbar, form validation classes.

---

## 12. SweetAlert2 Flash Message Convention

All controller operations follow POST → redirect → GET.
Flash messages are passed via `RedirectAttributes.addFlashAttribute("flashType", "success")`.

In `flash-messages.html` fragment, Thymeleaf checks for flash attributes and
renders an inline `<script>` block that fires `Swal.fire(...)` on page load.

```
flashType values: "success", "error", "warning", "info"
flashMessage values: the actual message string
```

This convention must be followed by every controller that redirects.

---

## 13. Sidebar Navigation Map

```
SkyLink Ops
├── Dashboard
├── Flights ▾
│   ├── Add New Flight
│   ├── Edit Flights (→ Flight List)
│   ├── Flight Schedule
│   └── Cancelled Flights (→ filtered Flight List)
├── Aircraft
│   ├── Add Aircraft (sub-item)
│   └── All Aircraft (sub-item)
├── Bookings
├── Find & Book  ← Receptionist primary action
├── Customers
├── Reports
│── Settings (bottom)
└── Log Out (bottom)
```

Active page highlights the correct nav item.
Sub-menus expand with Bootstrap 5 Collapse — no JS needed beyond Bootstrap.

---

## 14. Responsive Behavior

| Breakpoint | Behavior |
|------------|----------|
| Desktop (≥ 768px) | Sidebar visible, full layout |
| Tablet (< 768px) | Sidebar hidden, hamburger menu shows it as overlay |
| Mobile (< 576px) | Tables scroll horizontally, cards stack vertically |

Bootstrap 5 breakpoints handle all of this via `md:hidden` / `d-none d-md-flex` classes.

---

## 15. Naming Conventions

### CSS Classes
- Use descriptive BEM-lite: `.stat-card`, `.stat-card__value`, `.stat-card__label`
- Or utility override: `.card-hover`, `.badge-confirmed`, `.badge-pending`
- Never inline styles except for dynamic values passed from Thymeleaf

### Thymeleaf Fragments
- `th:fragment="sidebar"` — sidebar component
- `th:fragment="topbar(pageTitle)"` — topbar with dynamic title
- `th:fragment="flash"` — SweetAlert trigger
- `th:replace="~{layout/base :: content}"` — page content slot

### JavaScript
- All functions in named functions, no anonymous inline handlers
- SweetAlert wrappers: `confirmDelete(url)`, `showSuccess(msg)`, `showError(msg)`

---

## 16. Design Do's and Don'ts

### Do
- Use `--primary` (#1d0947) as the sidebar background always
- Use `--secondary` (#68519d) for all interactive elements (buttons, links, active states)
- Use `--outline-variant` (#cac4d0) for all card borders and table dividers
- Keep card padding at 24px (lg) for standard cards, 16px (md) for compact cards
- Show status always as a colored pill badge — never as plain text
- Use Material Symbols icon font throughout — consistent with the reference design
- Use `mono-data` style for all currency and numeric values in tables

### Don't
- Don't mix icon libraries — pick Material Symbols, use only that
- Don't use browser default `alert()` anywhere — always SweetAlert2
- Don't put business logic or large scripts in Thymeleaf HTML templates
- Don't duplicate sidebar and topbar HTML — always use Thymeleaf fragments
- Don't use `display-lg` (32px) font size anywhere except airport codes and hero numbers
- Don't add animations beyond Bootstrap's built-in transitions
- Don't use AOS, GSAP, or any animation library

---

## 17. Development Build Order

Design and implement pages in this order:

1. `main.css` — CSS variables + base styles
2. `fragments/sidebar.html` + `fragments/topbar.html`
3. `layout/base.html` — master layout
4. `auth/login.html` — standalone, no sidebar needed
5. `dashboard/index.html` — the showcase page
6. `flights/list.html` → `flights/form.html`
7. `aircraft/list.html` → `aircraft/form.html`
8. `bookings/list.html` → `bookings/detail.html`
9. `bookings/search.html` → `bookings/create.html`
10. `customers/list.html` → `customers/form.html` → `customers/detail.html`
11. `reports/index.html`
12. `error/403.html` + `error/404.html`

---

*This document is the single source of truth for all UI/UX decisions in this project.
Any new page added must follow the color system, typography scale, spacing,
component specs, and naming conventions defined here.*
