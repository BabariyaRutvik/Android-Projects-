# Implementation Plan - Saving Calculator Screen

Create the XML layout for the Saving Calculator activity, ensuring visual consistency with existing calculators like SIP and Loan.

## Proposed Changes

### [Resources]

#### [MODIFY] [strings.xml](file:///C:/Users/Rutvik/Android-Projects-/Task/Calculator/app/src/main/res/values/strings.xml)
- Add strings for Saving Calculator:
    - `saving_calculator`: "Saving Calculator"
    - `saving_goal`: "Saving Goal"
    - `monthly_saving_amount`: "Monthly Saving Amount"
    - `interest_earned`: "Interest Earned"
    - `daily_saving`: "Daily Saving"

### [Layouts]

#### [MODIFY] [activity_saving_calculator.xml](file:///C:/Users/Rutvik/Android-Projects-/Task/Calculator/app/src/main/res/layout/activity_saving_calculator.xml)
- Implement the layout using `ConstraintLayout`.
- Add a custom toolbar with a back button.
- Add a `NestedScrollView` for the main content.
- **Input Section**:
    - Create boxes for "Saving Goal", "Rate(%)", and "Saving Period".
    - Use consistency in padding, font sizes (15sp for labels, 18sp for inputs), and background (`@drawable/sl_input_bg`).
    - Use the orange cursor (`@drawable/orange_cursor`).
- **Result Section**:
    - Create a result card (`@drawable/bg_result`) with a "Result" divider.
    - Display "Monthly Saving Amount" in large orange text (35sp).
    - Display "Total Amount", "Total Invested", "Interest Earned", and "Daily Saving" with silver dividers.
- **Keypad Section**:
    - Include the standard numeric keypad.
    - Include "AC", Backspace, and a orange "=" button spanning two rows.

## Verification Plan

### Manual Verification
- Open the Saving Calculator screen.
- Verify the toolbar title and back button.
- Check that the input boxes match the style and size of the SIP/Loan screens.
- Verify the result section layout, including font sizes and colors.
- Ensure the keypad is correctly positioned at the bottom.
