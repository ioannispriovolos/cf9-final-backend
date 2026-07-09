-- 3. Seed your 3 System Users using PostgreSQL's native UUID generation
-- Password hashes below are all the BCrypt hash for: password123
INSERT INTO users (id, uuid, username, password, role_id, created_at, updated_at, deleted, deleted_at)
VALUES
    (
        1,
        gen_random_uuid(),
        'admin_user',
        '$2a$12$R9h/cIPz0gi9VloS.gA81u7Kuxv0S6vXfAnP.5K6xK6/bby2pS9vC',
        1,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        FALSE,
        NULL
    ),
    (
        2,
        gen_random_uuid(),
        'engineer_user',
        '$2a$12$R9h/cIPz0gi9VloS.gA81u7Kuxv0S6vXfAnP.5K6xK6/bby2pS9vC',
        2,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        FALSE,
        NULL
    ),
    (
        3,
        gen_random_uuid(),
        'viewer_user',
        '$2a$12$R9h/cIPz0gi9VloS.gA81u7Kuxv0S6vXfAnP.5K6xK6/bby2pS9vC',
        3,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        FALSE,
        NULL
    );

-- 4. Correct your Identity column counter so future user creations don't collide
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;