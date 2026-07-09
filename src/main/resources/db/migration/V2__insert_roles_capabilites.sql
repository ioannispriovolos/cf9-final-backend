-- Insert roles
INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('NETWORK_ENGINEER'),
    ('VIEWER');

-- Insert capabilities
INSERT INTO capabilities (name, description)
VALUES
    ('INSERT_USER',   'Create a new user'),
    ('VIEW_USERS',    'View user list and details'),
    ('VIEW_USER',     'View user'),
    ('EDIT_USER',     'Modify existing user'),
    ('DELETE_USER',   'Remove a user'),
    ('INSERT_DEVICE', 'Create a new network device'),
    ('EDIT_DEVICE',   'Modify existing network device'),
    ('DELETE_DEVICE', 'Remove a network device'),
    ('VIEW_ONLY_USER','View only inventory and metrics');

-- Assign all capabilities to ADMIN
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'ADMIN';

-- Assign INSERT_DEVICE, EDIT_DEVICE and DELETE_DEVICE to NETWORK_ENGINEER
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'NETWORK_ENGINEER'
  AND c.name IN ('INSERT_DEVICE', 'EDIT_DEVICE', 'DELETE_DEVICE');

-- Assign VIEW_ONLY_USER to VIEWER
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'VIEWER'
  AND c.name IN ('VIEW_ONLY_USER');