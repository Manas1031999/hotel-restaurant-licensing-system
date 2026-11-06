# নিজে হাতে চালিয়ে দেখার গাইড

এই গাইড ধরে চললে project-এর প্রতিটা feature নিজের চোখে দেখতে পাবি: JWT, role, ownership, workflow, status history, audit, notification, metrics আর logs। প্রতিটা ধাপে লেখা আছে কী দেখবি, কোথায় দেখবি, আর পেছনের code কোন file-এ।

---

## ০. যা লাগবে (একবারই)

| জিনিস | যাচাই |
|---|---|
| Java 21, PostgreSQL (`wb_nhls` database) | আগে থেকেই আছে |
| Docker Desktop | খুললে নিচে সবুজ **Engine running** দেখাবে |
| Mailpit | নতুন terminal-এ `mailpit version` |
| Bruno | তোর আগে থেকেই আছে |

---

## ১. সব চালু কর

**Terminal ১: Docker container আর Mailpit**

```powershell
cd "C:\Users\manas\Downloads\Hotel & Restaurant Licensing System\wb-nhls"
.\scripts\dev-up.ps1
```

এটা Prometheus, Loki আর Grafana চালু করে, আর Mailpit আলাদা একটা ছোট window-এ চালায়। শেষে কোথায় কী খুলবে সেটাও দেখিয়ে দেয়।

> "running scripts is disabled" error এলে একবার এটা চালা:
> `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`

**Terminal ২: App চালু**

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=observability
```

`Started WbNhlsApplication` দেখালে app তৈরি। VS Code থেকে চালাতে চাইলে run configuration-এ environment variable দে: `SPRING_PROFILES_ACTIVE=observability`।
`observability` profile না দিলে app চলবে, কিন্তু log Loki-তে যাবে না।

App চালু হওয়ার সময় যা হয়, console-এ খেয়াল কর:
- `Successfully validated` অথবা `Schema "public" is up to date`: **Flyway** schema মিলিয়ে দেখল
- `Default workflow নিয়ম তৈরি হয়েছে`: workflow-এর নিয়ম DB-তে বসল (শুধু প্রথমবার)

---

## ২. Bruno-তে collection খোল

1. Bruno খোল, তারপর **Open Collection**।
2. Project-এর `bruno\nhls-hands-on` folder বেছে নে।
3. উপরে ডানদিকে environment-এর জায়গায় **Local** বেছে নে।

Folder-গুলো এক এক করে, উপর থেকে নিচে চালাবি। কোনো folder-এ right-click করে **Run** দিলে ওই folder-এর সব request একসাথে চলবে, আর প্রতিটা test সবুজ (pass) না লাল (fail) সেটা দেখাবে। পুরো collection একবারে চালাতে চাইলে collection-এ right-click করে **Run** দে।

প্রতিটা request-এর **Tests** tab-এ দেখবি কী আশা করা হচ্ছে। Token আর ID নিজে থেকেই পরের request-এ চলে যায়, কিছু কপি-পেস্ট করতে হবে না।

> **নিয়ম:** সবসময় আগে `0-setup` চালাবি। Token-এর মেয়াদ ৬০ মিনিট। ৪০১ আসতে শুরু করলে `0-setup` আবার চালা। প্রতিবার চালালে নতুন user আর business তৈরি হয়, তাই যতবার খুশি চালাতে পারিস।

---

## ৩. Folder ধরে ধরে কী শিখবি

### `0-setup`: user তৈরি
- Admin login করে token নেয়, তারপর একজন **reviewer** আর একজন **approver** বানায়।
- দুজন applicant register করে: **rahim** আর **karim**।
- প্রত্যেকের token আলাদা variable-এ রেখে দেয়।

**কোথায় দেখবি:** DB-তে চালা `SELECT username, role FROM app_users ORDER BY id DESC LIMIT 5;`। Password plain text-এ নয়, BCrypt hash হিসেবে আছে: `SELECT password_hash FROM app_users LIMIT 1;`

**Code:** `user/service/AuthService.java`, `user/service/TokenService.java`, `config/JwtConfig.java`

### `1-auth-roles`: JWT আর role
| Request | ফল | কেন |
|---|---|---|
| No token | 401 | Token ছাড়া ঢোকা যায় না |
| Wrong password | 401 | User না থাকলেও একই বার্তা আসে, একই সময় লাগে |
| Applicant creates user | 403 | Staff শুধু ADMIN বানাতে পারে |
| Who am I | 200 | Token থেকে বোঝা যায় কে login করে আছে |

**নিজে করে দেখ:** login-এর response থেকে `accessToken` কপি করে https://jwt.io-তে paste কর। ভেতরে `sub` (username), `roles`, `exp` (মেয়াদ) দেখা যাবে।

**Code:** `config/SecurityConfig.java` (কোন URL কে ব্যবহার করতে পারবে), `config/JsonSecurityErrorHandler.java` (401/403-এর JSON)

### `2-business-ownership`: কার জিনিস কে দেখবে
- rahim একটা HOTEL register করে। এর owner এখন rahim।
- karim সেটা চাইলে **404** পায়, 403 নয়। ফলে সে বুঝতেও পারে না যে এই business আছে।
- karim-এর তালিকা ফাঁকা আসে, আর reviewer (staff) সবারটা দেখতে পায়।

**কোথায় দেখবি:** `SELECT b.business_name, u.username FROM businesses b JOIN app_users u ON u.id = b.owner_id ORDER BY b.id DESC LIMIT 3;`

**Code:** `user/service/CurrentUser.java` → `canAccess()`; `business/service/BusinessService.java` → `findAccessible()`

### `3-application-workflow`: নিয়ম মেনে review
| Request | ফল | কোন নিয়ম |
|---|---|---|
| karim, rahim-এর business দিয়ে আবেদন | 404 | ownership |
| rahim আবেদন জমা দেয় | 201, PENDING | |
| Reviewer-এর available actions | `["UNDER_REVIEW"]` | DB-র `workflow_transition` |
| Approver সরাসরি APPROVED দেয় | **400** | ধাপ বাদ দেওয়া যায় না |
| Reviewer UNDER_REVIEW দেয় | 200 | |
| Reviewer APPROVED দেয় | **403** | approve শুধু APPROVER পারে |
| Approver APPROVED দেয় | 200 | |
| Status history | ৩টা ধাপ, প্রতিটায় `changedBy` | |

**কোথায় দেখবি:**
- নিয়মগুলো: `SELECT business_type, from_status, to_status, required_role, active FROM workflow_transition;`
- ইতিহাস: `SELECT previous_status, new_status, changed_by, changed_at FROM application_status_history ORDER BY id DESC LIMIT 3;`
- **Mailpit** (http://127.0.0.1:8025): rahim-এর ঠিকানায় "আবেদন জমা হয়েছে" আর "অবস্থা বদলেছে" email এসেছে। ব্যর্থ চেষ্টা (400/403)-র জন্য কোনো email যায়নি।

**Code:** `workflow/service/WorkflowService.java` → `checkTransition()`; `license/service/LicenseApplicationService.java` → `reviewApplication()`

### `4-license-payment`
- Approver license জারি করে, তারপর rahim নিজে renew করে।
- karim, rahim-এর license renew করতে চাইলে 404 পায়।
- Payment শুরু হয় (PENDING), তারপর সম্পূর্ণ হয় (SUCCESS)। একই payment দুবার সম্পূর্ণ করতে গেলে **409** আসে।

**কোথায় দেখবি:** Mailpit-এ "লাইসেন্স জারি হয়েছে" আর "নবীকরণ হয়েছে" email।

### `5-workflow-rules`: code না বদলেই নিয়ম বদলানো
- Admin RESORT-এর জন্য বাড়তি একটা নিয়ম যোগ করে: `UNDER_REVIEW → INSPECTION`।
- তারপর নিয়মটা আবার বন্ধ করে দেয়। মুছে দেয় না, তাই ইতিহাস থেকে যায়।
- Reviewer নিয়ম বদলাতে চাইলে 403 পায়।

**নিজে করে দেখ:**
1. `02` চালানোর পরে `03` চালাবি না।
2. Bruno-তে একটা RESORT business বানিয়ে আবেদন কর, reviewer দিয়ে UNDER_REVIEW কর।
3. এবার available actions-এ `INSPECTION` দেখাবে, আর approver সরাসরি approve করতে পারবে না।
4. শেষে `03` চালিয়ে নিয়মটা বন্ধ করে দিস।

**Code:** `workflow/` package

### `6-audit-notifications`: কে কী করল
- Application-এর audit-এ সফল আর ব্যর্থ, দুই ধরনের চেষ্টাই আছে, আর প্রতিটায় আসল username।
- `outcome=FAILURE` দিলে শুধু ব্যর্থগুলো আসে: ভুল login, ভুল role, ভুল ধাপ।
- Outbox-এ প্রতিটা email-এর অবস্থা দেখা যায়: `SENT`, কতবার চেষ্টা হলো, কখন পাঠানো হলো।

**কোথায় দেখবি:**
- `SELECT action, outcome, actor, error_message FROM audit_log ORDER BY id DESC LIMIT 10;`
- `SELECT template, recipient, status, attempts, sent_at FROM notification_outbox ORDER BY id DESC LIMIT 5;`

**Code:** `audit/aspect/AuditAspect.java`, `audit/listener/AuditLogListener.java`, `notification/aspect/NotifyAspect.java`, `notification/dispatcher/OutboxDispatcher.java`

**Retry নিজে দেখ:**
1. Mailpit-এর window বন্ধ কর (অথবা `.\scripts\dev-down.ps1` চালা)।
2. Bruno-তে `3-application-workflow` আবার চালা।
3. `/api/v1/notifications` দেখ: email-গুলো `PENDING` থাকবে, `attempts` বাড়বে, আর `lastError`-এ connection error দেখাবে।
4. Mailpit আবার চালু কর। কিছুক্ষণের মধ্যেই নিজে থেকে `SENT` হয়ে যাবে। Outbox pattern-এর মূল কথা এটাই: **কোনো email হারায় না।**

### `7-metrics`
- `/actuator/prometheus` token ছাড়াই খোলে, আর ওতে আমাদের নিজেদের metric আছে।
- বাকি actuator endpoint (যেমন `/actuator/env`) বন্ধ থাকে।

---

## ৪. Grafana-তে দেখ

1. http://localhost:3000 খোল, login কর `admin` / `admin` দিয়ে। Password বদলাতে বললে Skip দিতে পারিস।
2. **Dashboards → NHLS → NHLS - Licensing System Overview** খোল।
3. Bruno-তে পুরো collection দুই-তিনবার চালা, তারপর ৩০ সেকেন্ড অপেক্ষা কর।

| Panel | কী দেখবি |
|---|---|
| Applications submitted | কতগুলো আবেদন জমা পড়ল |
| Workflow transitions | কতবার UNDER_REVIEW আর APPROVED হলো |
| Notifications | কতগুলো email পাঠানো হলো |
| Requests / p95 latency | কোন API কতবার চলল, আর কত সময় নিল |
| Responses by status code | 200, 201, 400, 401, 403, 404, 409-এর হিসাব |
| JVM / Threads / DB pool | memory, thread আর DB connection |
| Warnings and errors | Bruno-র ব্যর্থ চেষ্টাগুলো `AUDIT FAILED` হিসেবে |

**Logs নিজে খোঁজ:** বাঁদিকের menu থেকে **Explore** খোল, উপরে datasource হিসেবে **Loki** বেছে এই query-গুলো চালা:

```
{app="wb-nhls"} |= "AUDIT"
{app="wb-nhls", level="WARN"}
{app="wb-nhls"} |= "rahim_"
```

> Dashboard শুধু শেষ ৩০ মিনিটের data দেখায়। কিছু না চালালে graph সমান রেখা আর log "No data" দেখাবে, এটা স্বাভাবিক।

---

## ৫. Automated test চালাও

Docker চালু থাকলে:

```powershell
.\mvnw.cmd test
```

Test চলার সময় Docker-এ একটা নতুন Postgres চালু হয়, Flyway সেখানে schema বানায়, তারপর পুরো flow যাচাই হয়। ফলাফলে `Tests run: 5, Failures: 0` দেখা উচিত। তোর আসল DB-তে এই test কোনো হাত দেয় না।

**Code:** `src/test/java/in/wb/nhls/LicenseWorkflowIntegrationTest.java`

---

## ৬. Flyway দেখ

```sql
SELECT version, description, type, success FROM flyway_schema_history;
```

তোর DB-তে `1 | << Flyway Baseline >>` দেখাবে, কারণ পুরনো DB-কে baseline হিসেবে নেওয়া হয়েছিল। নতুন DB-তে দেখাবে `1 | baseline | SQL`। Schema-র পুরো SQL আছে `src/main/resources/db/migration/V1__baseline.sql`-এ।

---

## ৭. বন্ধ কর

App-এর terminal-এ `Ctrl+C`, তারপর:

```powershell
.\scripts\dev-down.ps1
```

---

## সমস্যা হলে

| সমস্যা | কারণ ও সমাধান |
|---|---|
| `mailpit` চালালে "address already in use" | Mailpit আগে থেকেই চলছে। Task Manager-এ `mailpit` বন্ধ কর, অথবা `dev-down.ps1` চালা |
| `mailpit` command পাওয়া যাচ্ছে না | Install-এর পরে নতুন terminal খোল |
| App চালু হচ্ছে না, "Port 8080 was already in use" | আরেকটা app চলছে। IDE-তে আগেরটা বন্ধ কর |
| App চালু হচ্ছে না, "Schema-validation" error | Entity বদলেছিস কিন্তু migration লিখিসনি। নতুন `V2__...sql` লাগবে |
| Bruno-তে সব request 401 | Token-এর মেয়াদ শেষ। `0-setup` আবার চালা |
| Grafana-য় Loki-র log নেই | App `observability` profile ছাড়া চলছে, অথবা শেষ ৩০ মিনিটে কিছু হয়নি |
| `dev-up.ps1`: "Docker engine চলছে না" | Docker Desktop খুলে Engine running হওয়া পর্যন্ত অপেক্ষা কর |
| `mvnw test`-এ test skip হচ্ছে | Docker চলছে না |
