INSERT INTO accounts (account_id, holder_name, balance, status, version, last_updated)
VALUES (101, 'Kaushik', 10000.00, 'ACTIVE', 0, NOW())
ON DUPLICATE KEY UPDATE holder_name=VALUES(holder_name), balance=VALUES(balance),
status=VALUES(status), last_updated=NOW();

INSERT INTO accounts (account_id, holder_name, balance, status, version, last_updated)
VALUES (102, 'Vijay', 5000.00, 'ACTIVE', 0, NOW())
ON DUPLICATE KEY UPDATE holder_name=VALUES(holder_name), balance=VALUES(balance),
status=VALUES(status), last_updated=NOW();

-- Use LOCKED or CLOSED instead of INACTIVE
INSERT INTO accounts (account_id, holder_name, balance, status, version, last_updated)
VALUES (103, 'Inactive User', 2000.00, 'LOCKED', 0, NOW())
ON DUPLICATE KEY UPDATE holder_name=VALUES(holder_name), balance=VALUES(balance),
status=VALUES(status), last_updated=NOW();


