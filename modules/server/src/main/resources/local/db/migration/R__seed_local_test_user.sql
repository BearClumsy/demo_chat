-- Local-only seed data. This folder is on Flyway's `locations` ONLY under the `local` profile
-- (spring.flyway.locations in application-local.properties) and for `flywayMigrate` when it targets
-- the default local Postgres (see the `flyway {}` block in modules/server/build.gradle). It is never
-- on the classpath location list for staging/prod.
--
-- Repeatable (R__) rather than versioned so it can't collide with a future V2 in db/migration and
-- re-applies harmlessly if edited. Keep it idempotent.
--
-- Fixed test user for manual API calls: login `testuser`, password `password`
-- (BCrypt, strength 10), id 00000000-0000-0000-0000-000000000001.
INSERT INTO demo_chat.users (id, first_name, last_name, email, phone, login, password)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Test',
    'User',
    'test@example.com',
    NULL,
    'testuser',
    '$2a$10$aQ8.pjG.snir7mSyo4VtS.OIiJqR29RXeUlSZ6iRru4zhK1Rtw1Bi'
)
ON CONFLICT (login) DO NOTHING;
