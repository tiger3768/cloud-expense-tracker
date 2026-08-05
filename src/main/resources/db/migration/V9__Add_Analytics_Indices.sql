CREATE INDEX idx_expense_user_date
ON expenses(user_id,expense_date);

CREATE INDEX idx_expense_category
ON expenses(category);

CREATE INDEX idx_expense_type
ON expenses(type);