CREATE OR REPLACE PACKAGE BODY Order_Pkg AS

    -- Create a new order and linked address
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

DBMS_OUTPUT.PUT_LINE('✅ Order successfully created for: ' || p_name);
EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            DBMS_OUTPUT.PUT_LINE('⚠ Duplicate address entry detected.');
WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('❌ Error in Create_Order: ' || SQLERRM);
            RAISE;
END Create_Order;

    -- Retrieve orders based on company-level access
    FUNCTION Get_Orders_By_Access_Level(p_company_level IN VARCHAR2)
    RETURN Order_Info_Table PIPELINED
    AS
        v_visible_levels SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST();
BEGIN
CASE UPPER(p_company_level)
            WHEN 'GLOBAL' THEN
                v_visible_levels := SYS.ODCIVARCHAR2LIST('Global');
WHEN 'NATIONAL' THEN
                v_visible_levels := SYS.ODCIVARCHAR2LIST('National', 'Global');
WHEN 'REGIONAL' THEN
                v_visible_levels := SYS.ODCIVARCHAR2LIST('Regional', 'National', 'Global');
WHEN 'LOCAL' THEN
                v_visible_levels := SYS.ODCIVARCHAR2LIST('Local', 'Regional', 'National', 'Global');
ELSE
                RAISE_APPLICATION_ERROR(-20001, 'Invalid company level: ' || p_company_level);
END CASE;

FOR rec IN (
            SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
                   o.company_name, a.name, a.street, a.city, a.state
            FROM Orders o
            JOIN Order_Address a ON o.address_id = a.address_id
            WHERE o.company_name IN (SELECT COLUMN_VALUE FROM TABLE(v_visible_levels))
        ) LOOP
            PIPE ROW (Order_Info_Obj(
                rec.order_id, rec.item, rec.price, rec.order_date, rec.status,
                rec.view_level, rec.company_name, rec.name, rec.street, rec.city, rec.state
            ));
END LOOP;
        RETURN;
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('❌ Error in Get_Orders_By_Access_Level: ' || SQLERRM);
            RAISE;
END;

    -- Return orders and books for a specific company
    FUNCTION Get_Orders_By_Company(p_company_name IN VARCHAR2)
    RETURN SYS_REFCURSOR
    AS
        rc SYS_REFCURSOR;
BEGIN
OPEN rc FOR
SELECT o.order_id, o.item, o.price, o.order_date, o.status, o.view_level,
       o.company_name, a.name, a.street, a.city, a.state, cb.book_title
FROM Orders o
         JOIN Order_Address a ON o.address_id = a.address_id
         JOIN Company_Books cb ON o.company_name = cb.company_name
WHERE o.company_name = p_company_name;
RETURN rc;
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('❌ Error in Get_Orders_By_Company: ' || SQLERRM);
            RAISE;
END;

    -- Update the status of a specific order
    PROCEDURE Update_Order_Status(p_order_id IN NUMBER, p_new_status IN VARCHAR2) AS
BEGIN
UPDATE Orders SET status = p_new_status WHERE order_id = p_order_id;

IF SQL%ROWCOUNT = 0 THEN
            DBMS_OUTPUT.PUT_LINE('⚠ No order found with ID: ' || p_order_id);
ELSE
            DBMS_OUTPUT.PUT_LINE('✅ Order ' || p_order_id || ' updated to status: ' || p_new_status);
END IF;
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('❌ Error in Update_Order_Status: ' || SQLERRM);
            RAISE;
END;

    -- Delete an order record by ID
    PROCEDURE Delete_Order(p_order_id IN NUMBER) AS
BEGIN
DELETE FROM Orders WHERE order_id = p_order_id;

IF SQL%ROWCOUNT = 0 THEN
            DBMS_OUTPUT.PUT_LINE('⚠ No order found to delete with ID: ' || p_order_id);
ELSE
            DBMS_OUTPUT.PUT_LINE('✅ Order ' || p_order_id || ' deleted.');
END IF;
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('❌ Error in Delete_Order: ' || SQLERRM);
            RAISE;
END;

END Order_Pkg;
/
