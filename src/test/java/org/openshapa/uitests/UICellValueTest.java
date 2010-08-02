package org.openshapa.uitests;

import java.awt.event.KeyEvent;

import java.io.File;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;

import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.fixture.SpreadsheetCellFixture;
import org.fest.swing.util.Platform;

import org.openshapa.util.KeysItem;
import org.openshapa.util.StringItem;
import org.openshapa.util.TextItem;
import org.openshapa.util.UIUtils;

import org.testng.Assert;

import org.testng.annotations.Test;


/**
 * Test for the New Cells.
 */
public final class UICellValueTest extends OpenSHAPATestClass {

    /**
     * Nominal test input.
     */
    private String[] nominalTestInput = {
            "Subject stands )up ", "$10,432", "Hand me (the manual!",
            "Tote_that_bale", "Jeune; fille celebre", "If x>7 then x|2"
        };

    /**
     * Text test input.
     */
    private String[] textTestInput = {
            "Subject stands up ", "$10,432", "Hand me the manual!",
            "Tote_that_bale", "Jeune fille celebre", "If x?7 then x? 2"
        };

    /**
     * Integer test input.
     */
    private String[] integerTestInput = {
            "1a9", "10-432", "!28.9(", "178&", "~~~)", "If x?7 then x? 2 ",
            "99999999999999999999", "000389.5", "-", "-0", "-123"
        };

    /**
     * Float test input.
     */
    private String[] floatTestInput = {
            "1a.9", "10-43.2", "!289(", "178.&", "0~~~)", "If x?7 then. x? 2 ",
            "589.138085638", "000389.5", "-0.1", "0.2", "-0.0", "-", "-0",
            "-.34", "-23.34", ".34", "12.34", "-123"
        };

    /**
     * Test creating a new NOMINAL cell.
     */
    @Test public void testNewNominalCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "n";
        String varType = "nominal";

        String[] expectedNominalTestOutput = {
                "Subject stands up", "$10432", "Hand me the manual!",
                "Tote_that_bale", "Jeune fille celebre", "If x7 then x2"
            };

        // 1. Create new variable
        mainFrameFixture.createNewVariable(varName, varType);

        runStandardTest(varName, nominalTestInput, expectedNominalTestOutput);
    }

    /**
     * Test pasting in Nominal cell.
     */
    @Test public void testNominalPasting() throws BadLocationException {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "n";
        String varRadio = "nominal";

        String[] expectedNominalTestOutput = {
                "Subject stands up", "$10432", "Hand me the manual!",
                "Tote_that_bale", "Jeune fille celebre", "If x7 then x2"
            };

        cutAndPasteTest(varName, varRadio, nominalTestInput,
            expectedNominalTestOutput);
    }

    /**
     * Test creating a new NOMINAL cell with more advanced input.
     */
    @Test public void testNewAdvancedNominalCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "n";
        String varRadio = "nominal";

        // advanced Input will be provided between testInput
        int[][] advancedInput = {
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT},
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT},
                {KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT},
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_DELETE, KeyEvent.VK_RIGHT
                },
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE
                }
            };

        String[] expectedTestOutput = {
                "Subject stands u$10432p ", "$1043Hand me the manual!2",
                "Hand me the manuaTote_that_balel",
                "Tote_that_aJeune fille celebrel", "If x7 then x2"
            };

        mainFrameFixture.createNewVariable(varName, varRadio);

        runAdvancedTest(varName, nominalTestInput, advancedInput,
            expectedTestOutput);
    }

    /**
     * Test creating a new TEXT cell.
     */
    @Test public void testNewTextCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "t";
        String varRadio = "text";

        String[] expectedTestOutput = textTestInput;

        // 1. Create new TEXT variable,
        mainFrameFixture.createNewVariable(varName, varRadio);

        runStandardTest(varName, textTestInput, expectedTestOutput);
    }

    /**
     * Test pasting in TEXT cell.
     */
    @Test public void testTextPasting() throws BadLocationException {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "t";
        String varRadio = "text";

        String[] expectedTestOutput = textTestInput;
        cutAndPasteTest(varName, varRadio, textTestInput, expectedTestOutput);
    }

    /**
     * Test pasting in INTEGER cell.
     */
    @Test public void testIntegerPasting() throws BadLocationException {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "i";
        String varRadio = "integer";

        String[] expectedTestOutput = {
                "19", "-43210", "289", "178", "<val>", "72",
                "999999999999999999", "3895", "<val>", "0", "-123"
            };
        cutAndPasteTest(varName, varRadio, integerTestInput,
            expectedTestOutput);
    }

    /**
     * Test creating a new TEXT cell with more advanced input.
     */
    @Test public void testNewAdvancedTextCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "t";
        String varRadio = "text";

        // advanced Input will be provided between testInput
        int[][] advancedInput = {
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT},
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT},
                {KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT},
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_DELETE, KeyEvent.VK_RIGHT
                },
                {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT}
            };

        String[] advancedExpectedOutput = {
                "Subject stands u$10,432p ", "$10,43Hand me the manual!2",
                "Hand me the manuaTote_that_balel",
                "Tote_that_aJeune fille celebrel",
                "Jeune fille celebreIf x?7 then x? 2"
            };

        mainFrameFixture.createNewVariable(varName, varRadio);

        runAdvancedTest(varName, textTestInput, advancedInput,
            advancedExpectedOutput);
    }

    /**
     * Test creating a new FLOAT cell.
     *
     * @throws java.lang.Exception
     *             on any error
     */
    @Test public void testNewFloatCell() throws Exception {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "f";
        String varRadio = "float";

        String[] expectedTestOutput = {
                "1.9", "-43.21", "289", "178", "0", "7.2", "589.138085",
                "389.5", "-0.1", "0.2", "0", "0", "0",
                "-0.34", "-23.34", "0.34", "12.34", "-123"
            };

        mainFrameFixture.createNewVariable(varName, varRadio);

        runStandardTest(varName, floatTestInput, expectedTestOutput);
    }

    /**
     * Test pasting with FLOAT cell.
     */
    @Test public void testFloatPasting() throws BadLocationException {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "f";
        String varRadio = "float";

        String[] expectedTestOutput = {
                "1.9", "-43.21", "289", "178", "0", "7.2", "589.138085",
                "389.5", "-0.1", "0.2", "0", "0", "0",
                "-0.34", "-23.34", "0.34", "12.34", "-123"
            };

        cutAndPasteTest(varName, varRadio, floatTestInput, expectedTestOutput);
    }

    /**
     * Test creating a new FLOAT cell with advanced input.
     */
    @Test public void testNewAdvancedFloatCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "f";
        String varRadio = "float";

        String[] testInput = {
                "1a.9", "10-43.2", "!289(", "178.&", "0~~~)",
                "If x?7 then.- x? 8", "-589.138085638", "12.3"
            };

        int[][] advancedInput = {
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT},
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT},
                {KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT},
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_DELETE, KeyEvent.VK_RIGHT
                },
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE
                },
                {KeyEvent.VK_RIGHT},
                {
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT
                }
            };

        String[] expectedTestOutput = {
                "-43.21109", "-43.289210", "2178.8", "70", "-87", "589.138085",
                "-589.138085"
            };

        mainFrameFixture.createNewVariable(varName, varRadio);

        runAdvancedTest(varName, testInput, advancedInput, expectedTestOutput);
    }

    /**
     * Test creating a new INTEGER cell.
     */
    @Test public void testNewIntegerCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "i";
        String varRadio = "integer";

        String[] expectedTestOutput = {
                "19", "-43210", "289", "178", "<val>", "72",
                "999999999999999999", "3895", "<val>", "0", "-123"
            };

        mainFrameFixture.createNewVariable(varName, varRadio);

        runStandardTest(varName, integerTestInput, expectedTestOutput);
    }

    /**
     * Test creating a new INTEGER cell with advanced input.
     */
    @Test public void testNewAdvancedIntegerCell() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        String varName = "i";
        String varRadio = "integer";

        String[] testInput = {
                "1a9", "10-432", "!289(", "178&", "If x?7. then x? 2", "17-8&",
                "()12.3"
            };

        // advanced Input will be provided between testInput
        int[][] advancedInput = {
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT},
                {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT},
                {KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT},
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT, KeyEvent.VK_DELETE, KeyEvent.VK_RIGHT
                },
                {
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE
                },
                {
                    KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_LEFT,
                    KeyEvent.VK_LEFT
                }
            };

        String[] expectedTestOutput = {
                "-4321019", "-43289210", "21788", "772", "-817", "-817"
            };

        mainFrameFixture.createNewVariable(varName, varRadio);

        runAdvancedTest(varName, testInput, advancedInput, expectedTestOutput);
    }

    /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testNewMatrixCellSingleArgNominal() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        // 1. Create new variables using script
        
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        // 2. Test single cell types
        // Test nominal
        String varName = "mN1";

        String[] expectedNominalTestOutput = {
                "Subject stands up", "$10432", "Hand me the manual!",
                "Tote_that_bale", "Jeune fille celebre", "If x7 then x2"
            };

        runStandardTest(varName, expectedNominalTestOutput,
            expectedNominalTestOutput, "<nominal>");
    }

    /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testNewMatrixCellSingleArgFloat() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        // 1. Create new variables using script
        
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        // 2. Test single cell types
        // Test integer
        String varName = "mF1";

        String[] expectedFloatTestOutput = {
                "1.9", "-43.21", "289", "178", "0", "7.2", "589.138085",
                "389.5", "-0.1", "0.2", "0", "0", "0", "-0.34", "-23.34",
                "0.34", "12.34", "-123"
            };

        runStandardTest(varName, floatTestInput, expectedFloatTestOutput,
            "<float>");
    }

    /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testNewMatrixCellSingleArgInteger() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        // 1. Create new variables using script
        
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        // 2. Test single cell types
        // Test integer
        String varName = "mI1";

        String[] expectedIntTestOutput = {
                "19", "-43210", "289", "178", "<int>", "72",
                "999999999999999999", "3895", "<int>", "0", "-123"
            };

        runStandardTest(varName, integerTestInput, expectedIntTestOutput,
            "<int>");
    }

    /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testNewMatrixCellDoubleArgInteger() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        /**
         * Integer test input.
         */
        String[] iTestInput = {"1a9", "10-432"};

        // 1. Create new variables using script
        
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        // 2. Test double cell type
        String varName = "mI2";

        String[] expectedInt2TestOutput = {"19", "-43210"};

        int numOfTests = iTestInput.length;

        // 2a. Test integer, only first arg
        for (int i = 0; i < numOfTests; i++) {
            expectedInt2TestOutput[i] = "(" + expectedInt2TestOutput[i]
                + ", <int2>)";
        }

        runStandardTest(varName, iTestInput, expectedInt2TestOutput,
            "(<int1>, <int2>)");

        // 2b. Recursively test all permutations of test input
        String[][][] testInput =
            new String[expectedInt2TestOutput.length]
            [expectedInt2TestOutput.length][2];

        // String[] expectedInt2bTempOutput =
        // {"19", "-43210", "289", "178", "<int1>", "72",
        // "999999999999999999", "3895", "<int1>", "0", "-123" };

        String[] expectedInt2bTempOutput = {"19", "-43210"};

        String[][] expectedInt2bTestOutput =
            new String[expectedInt2TestOutput.length]
            [expectedInt2TestOutput.length];

        for (int i = 0; i < numOfTests; i++) {

            for (int j = 0; j < numOfTests; j++) {
                testInput[i][j][0] = iTestInput[i];
                testInput[i][j][1] = iTestInput[j];

                if (expectedInt2bTempOutput[i].equals("<int2>")) {
                    expectedInt2bTestOutput[i][j] = "(<int1>" + ", "
                        + expectedInt2bTempOutput[j] + ")";
                } else if (expectedInt2bTempOutput[j].equals("<int1>")) {
                    expectedInt2bTestOutput[i][j] = "("
                        + expectedInt2bTempOutput[i] + ", <int2>)";
                } else {
                    expectedInt2bTestOutput[i][j] = "("
                        + expectedInt2bTempOutput[i] + ", "
                        + expectedInt2bTempOutput[j] + ")";
                }
            }
        }

        for (int i = 0; i < numOfTests; i++) {
            runMatrixTest(varName, testInput[i], expectedInt2bTestOutput[i]);
        }
    }

    /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testNewMatrixCellDoubleArgNominal() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        /**
         * Nominal test input.
         */
        String[] nomTestInput = {"Subject stands )up ", "$10,432"};

        // 1. Create new variables using script
        
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        // 2. Test double cell type
        // 2a. Test nominal
        String varName = "mN2";

        String[] expectedTestOutput = {"Subject stands up", "$10432"};

        int numOfTests = expectedTestOutput.length;

        for (int i = 0; i < numOfTests; i++) {
            expectedTestOutput[i] = "(" + expectedTestOutput[i]
                + ", <nominal2>)";
        }

        runStandardTest(varName, nomTestInput, expectedTestOutput,
            "(<nominal1>, <nominal2>)");

        // 2b. Recursively test all permutations of test input
        String[][][] testInput =
            new String[nomTestInput.length][nomTestInput.length][2];

        String[] expectedNominal2bTempOutput = {"Subject stands up", "$10432"};

        String[][] expectedNominal2bTestOutput =
            new String[expectedNominal2bTempOutput.length]
            [expectedNominal2bTempOutput.length];

        for (int i = 0; i < numOfTests; i++) {

            for (int j = 0; j < numOfTests; j++) {
                testInput[i][j][0] = nomTestInput[i];
                testInput[i][j][1] = nomTestInput[j];

                if (expectedNominal2bTempOutput[i].equals("<nominal2>")) {
                    expectedNominal2bTestOutput[i][j] = "(<nominal1>" + ", "
                        + expectedNominal2bTempOutput[j] + ")";
                } else if (expectedTestOutput[j].equals("<nominal1>")) {
                    expectedNominal2bTestOutput[i][j] = "("
                        + expectedNominal2bTempOutput[i] + ", <nominal2>)";
                } else {
                    expectedNominal2bTestOutput[i][j] = "("
                        + expectedNominal2bTempOutput[i] + ", "
                        + expectedNominal2bTempOutput[j] + ")";
                }
            }
        }

        numOfTests = nomTestInput.length;

        for (int i = 0; i < numOfTests; i++) {
            runMatrixTest(varName, testInput[i],
                expectedNominal2bTestOutput[i]);
        }
    }

    /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testNewMatrixCellDoubleArgFloat() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        /**
         * Float test input.
         */
        String[] fTestInput = {"1a.9", "10-43.2"};

        // 1. Create new variables using script
        
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        // 2. Test double cell type
        String varName = "mF2";

        String[] expectedFloat2TestOutput = {"1.9", "-43.21"};

        int numOfTests = fTestInput.length;

        // 2a. Test integer, only first arg
        for (int i = 0; i < numOfTests; i++) {
            expectedFloat2TestOutput[i] = "(" + expectedFloat2TestOutput[i]
                + ", <float2>)";
        }

        // 2b. Recursively test all permutations of test input
        String[][][] testInput =
            new String[expectedFloat2TestOutput.length]
            [expectedFloat2TestOutput.length][2];

        String[] expectedInt2bTempOutput = {"1.9", "-43.21"};

        String[][] expectedInt2bTestOutput =
            new String[expectedFloat2TestOutput.length]
            [expectedFloat2TestOutput.length];

        for (int i = 0; i < numOfTests; i++) {

            for (int j = 0; j < numOfTests; j++) {
                testInput[i][j][0] = fTestInput[i];
                testInput[i][j][1] = fTestInput[j];

                if (expectedInt2bTempOutput[i].equals("<float2>")) {
                    expectedInt2bTestOutput[i][j] = "(<float1>" + ", "
                        + expectedInt2bTempOutput[j] + ")";
                } else if (expectedInt2bTempOutput[j].equals("<float1>")) {
                    expectedInt2bTestOutput[i][j] = "("
                        + expectedInt2bTempOutput[i] + ", <float2>)";
                } else {
                    expectedInt2bTestOutput[i][j] = "("
                        + expectedInt2bTempOutput[i] + ", "
                        + expectedInt2bTempOutput[j] + ")";
                }
            }
        }

        for (int i = 0; i < numOfTests; i++) {
            runMatrixTest(varName, testInput[i], expectedInt2bTestOutput[i]);
        }
    }

        /**
     * Test creating a new MATRIX cell.
     */
    @Test public void testMatrixArgumentMouseNavigation() {
        System.err.println(new Exception().getStackTrace()[0].getMethodName());

        //Matrix name (first element) and arguments (remaining elements)
        String[] nomMatrix = {"mN2", "<nominal1>", "<nominal2>"};
        String[] floatMatrix = {"mF2", "<float1>", "<float2>"};
        String[] intMatrix = {"mI2", "<int1>", "<int2>"};
        String[] mixedMatrix1 = {"mM1", "<float>", "<int>", "<nominal>", "<text>"};
        String[] mixedMatrix2 = {"mM2", "<float1>", "<int1>", "<int2>", "<nominal1>", "<float2>", "<nominal2>"};

        String[][] matrixNames = {nomMatrix, floatMatrix, intMatrix, mixedMatrix1, mixedMatrix2};

        //Run Matrix demo file
        final File demoFile = new File(testFolder + "/ui/matrix_tests.rb");
        Assert.assertTrue(demoFile.exists(),
            "Expecting matrix_tests.rb to exist.");

        mainFrameFixture.runScript(demoFile);

        // Close script console
        mainFrameFixture.closeScriptConsoleOnFinish();

        spreadsheet = mainFrameFixture.getSpreadsheet();

        //Create a cell for each matrix type and navigate to each argument by
        //clicking on it
        for (String[] matrixCol : matrixNames) {
            createCell(matrixCol[0]);
            SpreadsheetCellFixture cell = spreadsheet.column(matrixCol[0]).cell(1);
            //Click on each element from last to first
            for (int i = matrixCol.length - 1; i > 0; i--) {                
                int charPos = cell.cellValue().text().indexOf(matrixCol[i]) + 2;
                try {
                    cell.clickToCharPos(SpreadsheetCellFixture.VALUE, charPos);
                } catch (BadLocationException ex) {
                    Logger.getLogger(UICellValueTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Assert.assertEquals(cell.cellValue().component().getSelectedText(), matrixCol[i]);
            }
        }
    }

    /**
     * Runs advanced tests.
     *
     * @param varName
     *            name of variable and therefore column header name
     * @param testInput
     *            array of test input
     * @param advancedInput
     *            extra advanced input
     * @param expectedTestOutput
     *            expected test output
     */
    private void runAdvancedTest(final String varName, final String[] testInput,
        final int[][] advancedInput, final String[] expectedTestOutput) {
        int numOfTests = testInput.length;

        // 1. Create new cells, check that they have been created
        for (int ordinal = 1; ordinal <= (numOfTests - 1); ordinal++) {
            createCell(varName);
            Assert.assertTrue(cellExists(varName, ordinal),
                "Expecting cell to have been created.");
            Assert.assertTrue(cellHasOnset(varName, ordinal, "00:00:00:000"),
                "Expecting cell to have onset of 00:00:00:000");
            Assert.assertTrue(cellHasOffset(varName, ordinal, "00:00:00:000"),
                "Expecting cell to have offset of 00:00:00:000");
            Assert.assertTrue(cellHasValue(varName, ordinal, "<val>"),
                "Expecting different blank cell value");

            // 2. Test different inputs as per specifications
            List<TextItem> inputs = new LinkedList<TextItem>();
            inputs.add(new StringItem(testInput[ordinal - 1]));
            inputs.add(new KeysItem(advancedInput[ordinal - 1]));
            inputs.add(new StringItem(testInput[ordinal]));

            changeCellValue(varName, ordinal, inputs);

            Assert.assertTrue(cellHasValue(varName, ordinal,
                    expectedTestOutput[ordinal - 1]),
                "Expecting different cell contents.");
        }
    }

    /**
     * Runs a double argument matrix test.
     *
     * @param varName
     *            name of variable and therefore column header name
     * @param testInput
     *            Array of arguments for matrix
     * @param expectedTestOutput
     *            expected test output
     */
    private void runMatrixTest(final String varName, final String[][] testInput,
        final String[] expectedTestOutput) {
        int numOfTests = testInput.length;

        // 1. Delete existing cells.
        deleteAllCells(varName);

        // 2. Create new cells, check that they have been created
        for (int ordinal = 1; ordinal < numOfTests; ordinal++) {
            createCell(varName);
            Assert.assertTrue(cellExists(varName, ordinal),
                "Expecting cell to have been created.");
            Assert.assertTrue(cellHasOnset(varName, ordinal, "00:00:00:000"),
                "Expecting cell to have onset of 00:00:00:000");
            Assert.assertTrue(cellHasOffset(varName, ordinal, "00:00:00:000"),
                "Expecting cell to have offset of 00:00:00:000");

            // 4. Test different inputs as per specifications
            changeCellMatrixValue(varName, ordinal, testInput[ordinal - 1]);

            spreadsheet = mainFrameFixture.getSpreadsheet();

            String[] actualValues = UIUtils.getArgsFromMatrix(
                    spreadsheet.column(varName).cell(ordinal).cellValue()
                        .text());
            String[] expectedValues = UIUtils.getArgsFromMatrix(
                    expectedTestOutput[ordinal - 1]);

            for (int i = 0; i < actualValues.length; i++) {
                Assert.assertTrue(UIUtils.equalValues(actualValues[i],
                        expectedValues[i]));
            }
        }
    }

    //
    /**
     * matrix test exclusively for single argument matrix tests.
     *
     * @param varName
     *            name of variable and therefore column header name
     * @param testInput
     *            array of test input
     * @param expectedTestOutput
     *            expected test output
     * @param customBlank
     *            customBlank second argument
     */
    private void runMatrixTest(final String varName, final String[] testInput,
        final String[] expectedTestOutput, final String customBlank) {
        String[][] matricisedInput = new String[testInput.length][2];

        for (int i = 0; i < testInput.length; i++) {
            matricisedInput[i][0] = testInput[i];
            matricisedInput[i][1] = "";
        }

        runMatrixTest(varName, matricisedInput, expectedTestOutput);
    }

    /**
     * Runs standard tests without advanced input, default custom blank used.
     *
     * @param varName
     *            name of variable and therefore column header name
     * @param testInput
     *            array of test input
     * @param expectedTestOutput
     *            expected test output
     */
    private void runStandardTest(final String varName, final String[] testInput,
        final String[] expectedTestOutput) {
        runStandardTest(varName, testInput, expectedTestOutput, "<val>");
    }

    /**
     * Runs standard tests without advanced input.
     *
     * @param varName
     *            name of variable and therefore column header name
     * @param testInput
     *            array of test input
     * @param expectedTestOutput
     *            expected test output
     * @param customBlank
     *            the placeholder if a value is blank
     */
    private void runStandardTest(final String varName, final String[] testInput,
        final String[] expectedTestOutput, final String customBlank) {

        int numOfTests = testInput.length;

        // 1. Create new cells, check that they have been created
        for (int ordinal = 1; ordinal <= numOfTests; ordinal++) {
            createCell(varName);
            Assert.assertTrue(cellExists(varName, ordinal),
                "Expecting cell to have been created.");
            Assert.assertTrue(cellHasOnset(varName, ordinal, "00:00:00:000"),
                "Expecting cell to have onset of 00:00:00:000");
            Assert.assertTrue(cellHasOffset(varName, ordinal, "00:00:00:000"),
                "Expecting cell to have offset of 00:00:00:000");
            Assert.assertTrue(cellHasValue(varName, ordinal, customBlank),
                "Expecting different blank cell value");

            // 2. Test different inputs as per specifications
            changeCellValue(varName, ordinal, testInput[ordinal - 1]);
            clickCell(varName, ordinal);
            Assert.assertTrue(cellHasValue(varName, ordinal,
                    expectedTestOutput[ordinal - 1]),
                "Expecting different cell value");

            //Test for BugzID1634: -0.0
            spreadsheet = mainFrameFixture.getSpreadsheet();
            Assert.assertFalse(spreadsheet.column(varName).cell(ordinal)
                    .cellValue().text().matches("-0.0{1,6}"));
        }
    }

    /**
     * Tests for pasting.
     *
     * @param varName
     *            variable name
     * @param varRadio
     *            radio for variable
     * @param testInput
     *            test input values
     * @param expectedTestOutput
     *            expected test output values
     */
    private void cutAndPasteTest(final String varName, final String varRadio,
        final String[] testInput, final String[] expectedTestOutput) throws BadLocationException {
        int numOfTests = testInput.length;

        spreadsheet = mainFrameFixture.getSpreadsheet();

        // 1. Create new variable
        mainFrameFixture.createNewVariable(varName, varRadio);

        // 2. Create new cells, check that they have been created
        for (int ordinal = 1; ordinal <= numOfTests; ordinal++) {
            createCell(varName);
            Assert.assertTrue(cellExists(varName, ordinal),
                "Expecting cell to have been created.");
        }

        // 3. Check copy pasting
        for (int ordinal = 1; ordinal <= numOfTests; ordinal++) {

            // Don't want to paste a value that is potentially already in the
            // cell
            int inputIndex = (ordinal + 2) % numOfTests;

            try {
                // Type value into another cell
                SpreadsheetCellFixture currCell = spreadsheet.column(varName).cell(inputIndex + 1);
                currCell.select(SpreadsheetCellFixture.VALUE, 0, currCell.cellValue().text().length());
                currCell.cellValue().enterText(testInput[inputIndex]);

                // Cut value
                int strlen = spreadsheet.column(varName).cell(inputIndex + 1)
                    .cellValue().text().length();
                spreadsheet.column(varName).cell(inputIndex + 1).select(
                    SpreadsheetCellFixture.VALUE, 0, strlen);
            } catch (BadLocationException ex) {
                Logger.getLogger(UICellValueTest.class.getName()).log(
                    Level.SEVERE, null, ex);
            }

            spreadsheet.column(varName).cell(inputIndex + 1).cellValue()
                .pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_X)
                    .modifiers(Platform.controlOrCommandMask()));

            // Check that it is now blank
            Assert.assertTrue(cellHasValue(varName, inputIndex + 1, "<val>"),
                "Expecting cell contents to be deleted.");

            // Check pasting cell has different value
            if (!cellHasValue(varName, ordinal, "<val>")) {
                Assert.assertFalse(cellHasValue(varName, ordinal,
                        expectedTestOutput[inputIndex]),
                    "Expecting cell contents to be deleted.");
            }

            // Paste new contents.
            pasteCellValue(varName, ordinal);

            // Check that cell contents are pasted in
            Assert.assertTrue(cellHasValue(varName, ordinal,
                    expectedTestOutput[inputIndex]),
                "Expecting different cell contents.");
        }
    }

    /**
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     */
    private void createCell(final String varName) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        final int numCells = spreadsheet.column(varName).numOfCells();

        if (numCells == 0) {
            spreadsheet.column(varName).click();
        } else {
            spreadsheet.column(varName).click();
            spreadsheet.column(varName).cell(numCells).fillSelectCell(true);
        }

        mainFrameFixture.clickMenuItemWithPath("Spreadsheet", "New Cell");
    }

    /**
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     */
    private void clickCell(final String varName, final int id) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        spreadsheet.column(varName).cell(id).fillSelectCell(true);
    }

    /**
     * @param varName
     *            column to test against, assumes that the column already exists
     * @param id
     *            cell ordinal value
     * @return true if the cell with ordinal 'id' exists, false otherwise
     */
    private boolean cellExists(final String varName, final int id) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        return id <= spreadsheet.column(varName).numOfCells();
    }

    /**
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param onset
     *            Should be in the format HH:mm:ss:SSS
     * @return boolean - true if has onset
     */
    private boolean cellHasOnset(final String varName, final int id,
        final String onset) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        return spreadsheet.column(varName).cell(id).onsetTimestamp().text()
            .equals(onset);
    }

    /**
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param offset
     *            Should be in the format HH:mm:ss:SSS
     * @return boolean - true if has offset
     */
    private boolean cellHasOffset(final String varName, final int id,
        final String offset) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        return spreadsheet.column(varName).cell(id).offsetTimestamp().text()
            .equals(offset);
    }

    /**
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param value
     *            expected cell string value
     * @return true if the cell contains the expected cell value, false
     *         otherwise
     */
    private boolean cellHasValue(final String varName, final int id,
        final String value) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        return UIUtils.equalValues(spreadsheet.column(varName).cell(id)
                .cellValue().text(), value);

    }

    /**
     * Change cell values using a string as the target value.
     *
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param value
     *            new cell value
     */
    private void changeCellValue(final String varName, final int id,
        final String value) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        spreadsheet.column(varName).cell(id).cellValue().selectAll().enterText(
            value);
    }

    /**
     * Change cell value using a list of inputs as the target value.
     *
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param inputs
     *            list of value inputs
     */
    private void changeCellValue(final String varName, final int id,
        final List<TextItem> inputs) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        JTextComponentFixture text = spreadsheet.column(varName).cell(id)
            .cellValue();

        text.click();

        for (TextItem input : inputs) {
            input.enterItem(text);
        }

    }

    /**
     * Change cell value using a string array of inputs as the target value,
     * where each input is separated by the key press defined as separator.
     *
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param values
     *            array of input values
     */
    private void changeCellMatrixValue(final String varName, final int id,
        final String[] values) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        SpreadsheetCellFixture cell = spreadsheet.column(varName).cell(id);
        JTextComponentFixture textField = cell.cellValue();

        for (int inputColumn = 1; inputColumn <= values.length; inputColumn++) {
            cell.fillSelectCell(true);

            // Tab to the cell value
            for (int positions = 2 + inputColumn; positions > 0; positions--) {
                mainFrameFixture.robot.pressAndReleaseKeys(KeyEvent.VK_TAB);

                // textField.pressAndReleaseKeys(KeyEvent.VK_TAB);
                textField.selectAll();
            }

            textField.enterText(values[inputColumn - 1]);
            mainFrameFixture.robot.pressAndReleaseKeys(KeyEvent.VK_TAB);

        }

    }

    /**
     * @param varName
     *            name of column that contains the cell, assumes that the column
     *            already exists.
     * @param id
     *            cell ordinal value, assumes that the cell already exists
     * @param value
     *            cell value to paste
     */
    private void pasteCellValue(final String varName, final int id) throws BadLocationException {
        spreadsheet = mainFrameFixture.getSpreadsheet();
        SpreadsheetCellFixture currCell = spreadsheet.column(varName).cell(id);
        //Select all if not already selected e.g. <val>
        currCell.cellValue().click();
        currCell.cellValue().selectAll();
        currCell.cellValue().pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_V).modifiers(
                    Platform.controlOrCommandMask()));
    }

    /**
     * Deletes all cells in a particular column.
     *
     * @param varName
     *            variable name.
     */
    private void deleteAllCells(final String varName) {
        spreadsheet = mainFrameFixture.getSpreadsheet();

        int numOfCells = spreadsheet.column(varName).numOfCells();

        if (numOfCells > 0) {
            spreadsheet.column(varName).click();


            for (int ordinal = 1; ordinal <= numOfCells; ordinal++) {
                spreadsheet.column(varName).cell(ordinal).fillSelectCell(true);
            }

            if (numOfCells > 1) {
                mainFrameFixture.clickMenuItemWithPath("Spreadsheet",
                    "Delete Cells");
            } else if (numOfCells == 1) {
                mainFrameFixture.clickMenuItemWithPath("Spreadsheet",
                    "Delete Cell");
            }
        }
    }
}