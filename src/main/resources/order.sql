-- Step 1: Drop Tables If They Exist --
BEGIN
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE Orders CASCADE CONSTRAINTS';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -942 THEN RAISE; END IF; -- Ignore "table not found"
END;

BEGIN
EXECUTE IMMEDIATE 'DROP TABLE Order_Address CASCADE CONSTRAINTS';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -942 THEN RAISE; END IF;
END;
END;
/

-- Step 2: Recreate Tables --
CREATE TABLE Order_Address (
                               address_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               name       VARCHAR2(100),
                               street     VARCHAR2(100),
                               city       VARCHAR2(50),
                               state      VARCHAR2(50)
);

CREATE TABLE Orders (
                        order_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        item         VARCHAR2(100),
                        price        NUMBER(10,2),
                        order_date   TIMESTAMP,
                        status       VARCHAR2(50),
                        view_level   VARCHAR2(20),  -- Public, Restricted, Private
                        address_id   NUMBER,
                        CONSTRAINT fk_address FOREIGN KEY (address_id)
                            REFERENCES Order_Address(address_id)
);

-- Step 3: Drop and Recreate Create_Order Procedure --
BEGIN
BEGIN
EXECUTE IMMEDIATE 'DROP PROCEDURE Create_Order';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;
END;
/

CREATE OR REPLACE PROCEDURE Create_Order(
    p_item        IN VARCHAR2,
    p_price       IN NUMBER,
    p_order_date  IN TIMESTAMP,
    p_status      IN VARCHAR2,
    p_view_level  IN VARCHAR2,
    p_name        IN VARCHAR2,
    p_street      IN VARCHAR2,
    p_city        IN VARCHAR2,
    p_state       IN VARCHAR2
) AS
    v_address_id NUMBER;
BEGIN
INSERT INTO Order_Address(name, street, city, state)
VALUES (p_name, p_street, p_city, p_state)
    RETURNING address_id INTO v_address_id;

INSERT INTO Orders(item, price, order_date, status, view_level, address_id)
VALUES (p_item, p_price, p_order_date, p_status, p_view_level, v_address_id);
END;
/

-- Step 4: Drop and Recreate Order_Info_Obj Type and Get_Order_By_View Function --
BEGIN
BEGIN
EXECUTE IMMEDIATE 'DROP FUNCTION Get_Order_By_View';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;

BEGIN
EXECUTE IMMEDIATE 'DROP TYPE Order_Info_Obj';
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -4043 THEN RAISE; END IF;
END;
END;
/

CREATE OR REPLACE TYPE Order_Info_Obj AS OBJECT (
    order_id    NUMBER,
    item        VARCHAR2(100),
    price       NUMBER(10,2),
    order_date  TIMESTAMP,
    status      VARCHAR2(50),
    view_level  VARCHAR2(20),
    name        VARCHAR2(100),
    street      VARCHAR2(100),
    city        VARCHAR2(50),
    state       VARCHAR2(50)
);
/

CREATE OR REPLACE FUNCTION Get_Order_By_View(
    p_order_id   IN NUMBER,
    p_view_type  IN VARCHAR2
) RETURN Order_Info_Obj
AS
    v_actual_view VARCHAR2(20);
    v_order_info  Order_Info_Obj;
BEGIN
SELECT view_level
INTO v_actual_view
FROM Orders
WHERE order_id = p_order_id;

IF (p_view_type = 'Public' AND v_actual_view = 'Public') OR
       (p_view_type = 'Restricted' AND v_actual_view IN ('Public', 'Restricted')) OR
       (p_view_type = 'Private') THEN

SELECT Order_Info_Obj(
               o.order_id,
               o.item,
               o.price,
               o.order_date,
               o.status,
               o.view_level,
               a.name,
               a.street,
               a.city,
               a.state
       )
INTO v_order_info
FROM Orders o
         JOIN Order_Address a ON o.address_id = a.address_id
WHERE o.order_id = p_order_id;

RETURN v_order_info;
ELSE
        RAISE_APPLICATION_ERROR(-20001, 'Access denied: insufficient view privileges.');
END IF;
END;
/

-- Sample Orders Using Create_Order Procedure --
BEGIN
    -- Public order: basic delivery
    Create_Order(
        p_item       => 'USB Webcam',
        p_price      => 45.00,
        p_order_date => TO_TIMESTAMP('2025-07-24 09:15:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_status     => 'Delivered',
        p_view_level => 'Public',
        p_name       => 'Taylor Brooks',
        p_street     => '31 Sunset Blvd',
        p_city       => 'Manchester',
        p_state      => 'NH'
    );

    -- Restricted order: pending shipment
    Create_Order(
        p_item       => 'Portable SSD',
        p_price      => 89.99,
        p_order_date => TO_TIMESTAMP('2025-07-25 13:30:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_status     => 'Pending',
        p_view_level => 'Restricted',
        p_name       => 'Jordan Fields',
        p_street     => '14 Grove Street',
        p_city       => 'Concord',
        p_state      => 'NH'
    );

    -- Private order: canceled item
    Create_Order(
        p_item       => 'Noise-Canceling Earbuds',
        p_price      => 65.00,
        p_order_date => TO_TIMESTAMP('2025-07-26 16:45:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_status     => 'Cancelled',
        p_view_level => 'Private',
        p_name       => 'Morgan Hayes',
        p_street     => '99 Birch Lane',
        p_city       => 'Nashua',
        p_state      => 'NH'
    );
END;

-- Sample Get Orders Calls using Get_Order_By_View Function

DECLARE
result Order_Info_Obj;
BEGIN
    result := Get_Order_By_View(1, 'Restricted');
    DBMS_OUTPUT.PUT_LINE('Item: ' || result.item);
    DBMS_OUTPUT.PUT_LINE('Status: ' || result.status);
    DBMS_OUTPUT.PUT_LINE('View Level: ' || result.view_level);
    DBMS_OUTPUT.PUT_LINE('Customer: ' || result.name || ', ' || result.city);
END;



Select * from orders;

Select Get_Order_By_View(1, 'Restricted') from dual;

--- Step 1: Define Object Type (if not already created) ---
CREATE OR REPLACE TYPE Order_Info_Obj AS OBJECT (
    order_id    NUMBER,
    item        VARCHAR2(100),
    price       NUMBER(10,2),
    order_date  TIMESTAMP,
    status      VARCHAR2(50),
    view_level  VARCHAR2(20),
    name        VARCHAR2(100),
    street      VARCHAR2(100),
    city        VARCHAR2(50),
    state       VARCHAR2(50)
);
/

--- Step 2: Define Table Type for Returning Multiple Orders ---
CREATE OR REPLACE TYPE Order_Info_Table AS TABLE OF Order_Info_Obj;
/

-- Step 3: Create Function Returning Order_Info_Table This function returns all orders visible under a
-- given access level, similar to your earlier Get_Order_By_View, but across all rows. --

CREATE OR REPLACE FUNCTION Get_Orders_By_View(
    p_view_type IN VARCHAR2
) RETURN Order_Info_Table
PIPELINED
AS
BEGIN
FOR rec IN (
        SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
               a.name, a.street, a.city, a.state
        FROM Orders o
        JOIN Order_Address a ON o.address_id = a.address_id
        WHERE (p_view_type = 'Public'    AND o.view_level = 'Public')
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
            rec.name,
            rec.street,
            rec.city,
            rec.state
        ));
END LOOP;

    RETURN;
END;
/

-- sample query --
SELECT * FROM TABLE(Get_Orders_By_View('Restricted'));

