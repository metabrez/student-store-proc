Select * from orders;

Select * from Company_Books;
Select * from order_address;



SELECT
    o.order_id,
    o.item,
    o.price,
    o.order_date,
    o.status,
    o.view_level,
    o.company_name,
    a.name AS customer_name,
    a.city,
    a.state,
    cb.book_title AS company_book
FROM Orders o
         JOIN Order_Address a ON o.address_id = a.address_id
         JOIN Company_Books cb ON o.company_name = cb.company_name
WHERE o.company_name = 'National';  -- Change to 'National', 'Regional', 'Local' as needed


SELECT * FROM Orders o
                  JOIN Order_Address a ON o.address_id = a.address_id;

SELECT * FROM Orders o
                  JOIN Company_Books cb ON o.company_name = cb.company_name;

SELECT DISTINCT company_name FROM Orders;
SELECT DISTINCT company_name FROM Company_Books;

-- count --
SELECT COUNT(*) FROM Orders;

-- Step 2: Reinsert Sample Orders with Company Data --
BEGIN
    Create_Order(
        p_item         => 'Oracle Dev Kit',
        p_price        => 150.00,
        p_order_date   => TO_TIMESTAMP('2025-07-27 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_status       => 'Delivered',
        p_view_level   => 'Public',
        p_company_name => 'Global',
        p_name         => 'Alice Johnson',
        p_street       => '24 Ocean Dr',
        p_city         => 'Boston',
        p_state        => 'MA'
    );

    Create_Order(
        p_item         => '.Net Core Framework',
        p_price        => 120.00,
        p_order_date   => TO_TIMESTAMP('2025-07-27 11:45:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_status       => 'Pending',
        p_view_level   => 'Restricted',
        p_company_name => 'National',
        p_name         => 'Bob Lee',
        p_street       => '88 Forest Ave',
        p_city         => 'Springfield',
        p_state        => 'MA'
    );
END;

--Distinct --
SELECT DISTINCT company_name FROM Orders;

-- Query for private access --
SELECT *
FROM TABLE(Get_Orders_By_View('Private'));

--- Bonus: Count Orders Per Company with Private View --
SELECT company_name, COUNT(*) AS order_count
FROM TABLE(Get_Orders_By_View('Private'))
GROUP BY company_name;

--- 1. Drop and Recreate the Object Type --
BEGIN
EXECUTE IMMEDIATE 'DROP TYPE Order_Info_Obj';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4043 THEN RAISE; END IF;
END;
/

--- fix the above issue with below command. Drop first every dependent --
BEGIN
    -- Drop function that depends on Order_Info_Table
BEGIN
EXECUTE IMMEDIATE 'DROP FUNCTION Get_Orders_By_View';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;

    -- Drop table type that depends on Order_Info_Obj
BEGIN
EXECUTE IMMEDIATE 'DROP TYPE Order_Info_Table';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;

    -- Drop object type
BEGIN
EXECUTE IMMEDIATE 'DROP TYPE Order_Info_Obj';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;
END;
/

--- Step 1: Drop Existing Types and Function (in order) ---

BEGIN
    -- Drop function
BEGIN
EXECUTE IMMEDIATE 'DROP FUNCTION Get_Orders_By_View';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;

    -- Drop table type
BEGIN
EXECUTE IMMEDIATE 'DROP TYPE Order_Info_Table';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;

    -- Drop object type
BEGIN
EXECUTE IMMEDIATE 'DROP TYPE Order_Info_Obj';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;
END;
/

-- Step 1 : Create or Replace Types (outside package) ---
-- Object type
CREATE OR REPLACE TYPE Order_Info_Obj AS OBJECT (
    order_id     NUMBER,
    item         VARCHAR2(100),
    price        NUMBER(10,2),
    order_date   TIMESTAMP,
    status       VARCHAR2(50),
    view_level   VARCHAR2(20),
    company_name VARCHAR2(20),
    name         VARCHAR2(100),
    street       VARCHAR2(100),
    city         VARCHAR2(50),
    state        VARCHAR2(50)
);
/

-- Table type
CREATE OR REPLACE TYPE Order_Info_Table AS TABLE OF Order_Info_Obj;
/

-- Step2: create package specification --
CREATE OR REPLACE PACKAGE Order_Pkg AS
    PROCEDURE Create_Order(
        p_item         IN VARCHAR2,
        p_price        IN NUMBER,
        p_order_date   IN TIMESTAMP,
        p_status       IN VARCHAR2,
        p_view_level   IN VARCHAR2,
        p_company_name IN VARCHAR2,
        p_name         IN VARCHAR2,
        p_street       IN VARCHAR2,
        p_city         IN VARCHAR2,
        p_state        IN VARCHAR2
    );

    FUNCTION Get_Orders_By_View(
        p_view_type IN VARCHAR2
    ) RETURN Order_Info_Table PIPELINED;
END Order_Pkg;
/

-- step3 create package body --
CREATE OR REPLACE PACKAGE BODY Order_Pkg AS

    PROCEDURE Create_Order(
        p_item         IN VARCHAR2,
        p_price        IN NUMBER,
        p_order_date   IN TIMESTAMP,
        p_status       IN VARCHAR2,
        p_view_level   IN VARCHAR2,
        p_company_name IN VARCHAR2,
        p_name         IN VARCHAR2,
        p_street       IN VARCHAR2,
        p_city         IN VARCHAR2,
        p_state        IN VARCHAR2
    ) AS
        v_address_id NUMBER;
BEGIN
INSERT INTO Order_Address(name, street, city, state)
VALUES (p_name, p_street, p_city, p_state)
    RETURNING address_id INTO v_address_id;

INSERT INTO Orders(item, price, order_date, status, view_level, company_name, address_id)
VALUES (p_item, p_price, p_order_date, p_status, p_view_level, p_company_name, v_address_id);
END Create_Order;

    FUNCTION Get_Orders_By_View(
        p_view_type IN VARCHAR2
    ) RETURN Order_Info_Table PIPELINED
    AS
BEGIN
FOR rec IN (
            SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
                   o.company_name,
                   a.name, a.street, a.city, a.state
            FROM Orders o
            JOIN Order_Address a ON o.address_id = a.address_id
            WHERE (p_view_type = 'Public'     AND o.view_level = 'Public')
               OR (p_view_type = 'Restricted' AND o.view_level IN ('Public', 'Restricted'))
               OR (p_view_type = 'Private')
        ) LOOP
            PIPE ROW (Order_Info_Obj(
                rec.order_id,
                rec.item,
                rec.price,
                rec.order_date,
                rec.status,
                rec.view_level,
                rec.company_name,
                rec.name,
                rec.street,
                rec.city,
                rec.state
            ));
END LOOP;

        RETURN;
END Get_Orders_By_View;

END Order_Pkg;
/

-- sample calls --
-- Insert sample order
BEGIN
    Order_Pkg.Create_Order(
        p_item         => 'Java Training Kit',
        p_price        => 180.00,
        p_order_date   => SYSTIMESTAMP,
        p_status       => 'Delivered',
        p_view_level   => 'Public',
        p_company_name => 'Global',
        p_name         => 'Sam Patel',
        p_street       => '500 Main St',
        p_city         => 'Manchester',
        p_state        => 'NH'
    );
END;
/

-- Retrieve private-access orders
SELECT * FROM TABLE(Order_Pkg.Get_Orders_By_View('Private'));


--- Final Package Specification ---

CREATE OR REPLACE PACKAGE Order_Pkg AS
    -- Insert an order
    PROCEDURE Create_Order(
        p_item         IN VARCHAR2,
        p_price        IN NUMBER,
        p_order_date   IN TIMESTAMP,
        p_status       IN VARCHAR2,
        p_view_level   IN VARCHAR2,
        p_company_name IN VARCHAR2,
        p_name         IN VARCHAR2,
        p_street       IN VARCHAR2,
        p_city         IN VARCHAR2,
        p_state        IN VARCHAR2
    );

    -- Access-control: public, restricted, private
    FUNCTION Get_Orders_By_View(
        p_view_type IN VARCHAR2
    ) RETURN Order_Info_Table PIPELINED;

    -- Company-based query
    FUNCTION Get_Orders_By_Company(
        p_company_name IN VARCHAR2
    ) RETURN SYS_REFCURSOR;

    -- Delete order
    PROCEDURE Delete_Order(p_order_id IN NUMBER);

    -- Update status
    PROCEDURE Update_Order_Status(p_order_id IN NUMBER, p_new_status IN VARCHAR2);
END Order_Pkg;
/


-- Final Package body ---
CREATE OR REPLACE PACKAGE BODY Order_Pkg AS

    PROCEDURE Create_Order(
        p_item         IN VARCHAR2,
        p_price        IN NUMBER,
        p_order_date   IN TIMESTAMP,
        p_status       IN VARCHAR2,
        p_view_level   IN VARCHAR2,
        p_company_name IN VARCHAR2,
        p_name         IN VARCHAR2,
        p_street       IN VARCHAR2,
        p_city         IN VARCHAR2,
        p_state        IN VARCHAR2
    ) AS
        v_address_id NUMBER;
BEGIN
INSERT INTO Order_Address(name, street, city, state)
VALUES (p_name, p_street, p_city, p_state)
    RETURNING address_id INTO v_address_id;

INSERT INTO Orders(item, price, order_date, status, view_level, company_name, address_id)
VALUES (p_item, p_price, p_order_date, p_status, p_view_level, p_company_name, v_address_id);
END Create_Order;

    FUNCTION Get_Orders_By_View(
        p_view_type IN VARCHAR2
    ) RETURN Order_Info_Table PIPELINED
    AS
BEGIN
FOR rec IN (
            SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
                   o.company_name, a.name, a.street, a.city, a.state
            FROM Orders o
            JOIN Order_Address a ON o.address_id = a.address_id
            WHERE (p_view_type = 'Public'     AND o.view_level = 'Public')
               OR (p_view_type = 'Restricted' AND o.view_level IN ('Public', 'Restricted'))
               OR (p_view_type = 'Private')
        ) LOOP
            PIPE ROW (Order_Info_Obj(
                rec.order_id, rec.item, rec.price, rec.order_date, rec.status,
                rec.view_level, rec.company_name, rec.name, rec.street, rec.city, rec.state
            ));
END LOOP;
        RETURN;
END Get_Orders_By_View;

    FUNCTION Get_Orders_By_Company(p_company_name IN VARCHAR2) RETURN SYS_REFCURSOR
    AS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
       o.company_name, a.name, a.street, a.city, a.state, cb.book_title
FROM Orders o
         JOIN Order_Address a ON o.address_id = a.address_id
         JOIN Company_Books cb ON o.company_name = cb.company_name
WHERE o.company_name = p_company_name;

RETURN v_cursor;
END Get_Orders_By_Company;

    PROCEDURE Delete_Order(p_order_id IN NUMBER) AS
BEGIN
DELETE FROM Orders WHERE order_id = p_order_id;
END Delete_Order;

    PROCEDURE Update_Order_Status(p_order_id IN NUMBER, p_new_status IN VARCHAR2) AS
BEGIN
UPDATE Orders SET status = p_new_status WHERE order_id = p_order_id;
END Update_Order_Status;

END Order_Pkg;
/


--- Sample usage ---
-- Delete an order
BEGIN
    Order_Pkg.Delete_Order(1);
END;
/

-- Update order status
BEGIN
    Order_Pkg.Update_Order_Status(2, 'Delivered');
END;
/

--- fix it by matching all columns ---

DECLARE
rc SYS_REFCURSOR;
    ord_id       NUMBER;
    item         VARCHAR2(100);
    price        NUMBER(10,2);
    order_date   TIMESTAMP;
    status       VARCHAR2(50);
    view_level   VARCHAR2(20);
    company_name VARCHAR2(20);
    name         VARCHAR2(100);
    street       VARCHAR2(100);
    city         VARCHAR2(50);
    state        VARCHAR2(50);
    book_title   VARCHAR2(100);
BEGIN
    rc := Order_Pkg.Get_Orders_By_Company('Global');
    LOOP
FETCH rc INTO ord_id, item, price, order_date, status, view_level,
                      company_name, name, street, city, state, book_title;
        EXIT WHEN rc%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Order #' || ord_id || ': ' || item ||
                             ' | Book: ' || book_title ||
                             ' | Company: ' || company_name);
END LOOP;
CLOSE rc;
END;
/


-- Get orders visible with 'Private' access
SELECT * FROM TABLE(Order_Pkg.Get_Orders_By_View('Private'));

-- Get company-specific orders with book info
DECLARE
rc SYS_REFCURSOR;
    ord_id NUMBER;
    item VARCHAR2(100);
    book_title VARCHAR2(100);
BEGIN
    rc := Order_Pkg.Get_Orders_By_Company('Global');
    LOOP
FETCH rc INTO ord_id, item,book_title;
        EXIT WHEN rc%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('Order ' || ord_id || ' | ' || item || ' | ' || book_title);
END LOOP;
CLOSE rc;
END;
/

--✅ Fix: Declare All Variables to Match the Cursor --
DECLARE
rc SYS_REFCURSOR;
    ord_id       NUMBER;
    item         VARCHAR2(100);
    price        NUMBER(10,2);
    order_date   TIMESTAMP;
    status       VARCHAR2(50);
    view_level   VARCHAR2(20);
    company_name VARCHAR2(20);
    name         VARCHAR2(100);
    street       VARCHAR2(100);
    city         VARCHAR2(50);
    state        VARCHAR2(50);
    book_title   VARCHAR2(100);
BEGIN
    rc := Order_Pkg.Get_Orders_By_Company('Global');
    LOOP
FETCH rc INTO ord_id, item, price, order_date, status, view_level,
                     company_name, name, street, city, state, book_title;

        EXIT WHEN rc%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Order #' || ord_id || ' | ' || item ||
                             ' | Book: ' || book_title ||
                             ' | Customer: ' || name || ', ' || city);
END LOOP;
CLOSE rc;
END;
/

-- cursor count --
SELECT COUNT(*)
FROM (
         SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
                o.company_name, a.name, a.street, a.city, a.state, cb.book_title
         FROM Orders o
                  JOIN Order_Address a ON o.address_id = a.address_id
                  JOIN Company_Books cb ON o.company_name = cb.company_name
         WHERE o.company_name = 'Global'
     );



-- Book and oder --
SELECT o.order_id, o.item, LISTAGG(cb.book_title, ', ') WITHIN GROUP (ORDER BY cb.book_title) AS books
FROM Orders o
    JOIN Company_Books cb ON o.company_name = cb.company_name
WHERE o.company_name = 'Global'
GROUP BY o.order_id, o.item;

-- Query: One Row Per Order with all Related info --
SELECT
    o.order_id,
    o.item,
    o.price,
    o.order_date,
    o.status,
    o.view_level,
    o.company_name,
    a.name AS customer_name,
    a.street,
    a.city,
    a.state,
    LISTAGG(cb.book_title, ', ') WITHIN GROUP (ORDER BY cb.book_title) AS company_books
FROM Orders o
    JOIN Order_Address a ON o.address_id = a.address_id
    JOIN Company_Books cb ON o.company_name = cb.company_name
WHERE o.order_id = 1  -- Replace with the specific order ID you want
GROUP BY
    o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
    o.company_name, a.name, a.street, a.city, a.state;

--- SQL: Standalone Delete Procedure ---

CREATE OR REPLACE PROCEDURE Delete_Order_By_Id (
    p_order_id IN NUMBER
) AS
BEGIN
DELETE FROM Orders WHERE order_id = p_order_id;

-- Optional: You may want to delete associated address if no other order uses it
-- DELETE FROM Order_Address WHERE address_id NOT IN (SELECT address_id FROM Orders);

DBMS_OUTPUT.PUT_LINE('Order ' || p_order_id || ' deleted.');
END;
/


--- usage --
BEGIN
    Delete_Order_By_Id(102);  -- Replace with actual order_id
END;
/
Select * from Orders where order_Id = 101


