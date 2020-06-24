/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SealedTypes15Tests extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug564498_1"};
	}

	public static Class<?> testClass() {
		return SealedTypes15Tests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public SealedTypes15Tests(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("15");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("15"));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("15") :
			JavacTestOptions.forReleaseWithPreview("15", javacAdditionalTestOptions);
		runner.runWarningTest();
	}

	@SuppressWarnings("unused")
	private static void verifyClassFile(String expectedOutput, String classFileName, int mode)
			throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}

	public void testBug563430_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class Y permits X{}\n" +
				"non-sealed class X extends Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n",
			},
			"0");
	}
	public void testBug563430_001a() {
		runConformTest(
			new String[] {
				"X.java",
				"non-sealed class X extends Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n",
				"Y.java",
				"sealed class Y permits X{}\n",
			},
			"0");
	}
	public void testBug563430_002() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I extends SI{}\n"+
				"non-sealed class X implements SI{\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"sealed interface SI permits X, I{}\n" +
				"non-sealed interface I2 extends I{}\n"
			},
			"0");
	}
	public void testBug562715_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     int sealed = 100;\n" +
				"     System.out.println(sealed);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_003() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_004() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"sealed public class X<T> {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y<T> extends X<T> {\n"+
				"}\n" +
				"non-sealed interface I2 extends I {}\n"
			},
			"100");
	}
	public void testBug562715_004a() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed public class X<T> {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	sealed public sealed class X {\n" +
			"	                           ^\n" +
			"Duplicate modifier for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	sealed public sealed class X {\n" +
			"	                           ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares X as its direct superclass\n" +
			"----------\n");
	}
	public void testBug562715_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X {\n" +
			"	                    ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares X as its direct superclass\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public static sealed void main(String[] args){\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug562715_007() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed @MyAnnot public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"@interface MyAnnot {}\n" +
				"non-sealed class Y extends X{}"
			},
			"100");
	}
	public void testBug562715_008() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"sealed class Y extends X {}\n" +
				"final class Z extends Y {}\n"
			},
			"100");
	}
	public void testBug562715_009() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y,Z {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"sealed class Y extends X {}\n" +
				"final class Z extends X {}\n" +
				"final class Y2 extends Y {}\n"
			},
			"100");
	}
	public void testBug562715_010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits {\n" +
			"	                    ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares X as its direct superclass\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits {\n" +
			"	                      ^^^^^^^\n" +
			"Syntax error on token \"permits\", ClassType expected after this token\n" +
			"----------\n");
	}
	// TODO : Enable after error flag code implemented
	public void _testBug562715_011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed enum Natural {ONE, TWO}\n"+
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	EXPECTED ERROR IN RECORD\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void _testBug562715_xxx() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed record R() {}\n"+
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	EXPECTED ERROR IN RECORD\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug563806_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits Y, Z{\n"+
				"}\n"+
				"class Y {}\n"+
				"class Z {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits Y, Z{\n" +
			"	                              ^\n" +
			"Permitted class Y does not declare X as direct super class\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits Y, Z{\n" +
			"	                                 ^\n" +
			"Permitted class Z does not declare X as direct super class\n" +
			"----------\n");
	}
	public void testBug563806_002() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y{\n"+
				"}\n"+
				"class Y {}\n"+
				"class Z extends X{}",
				"p1/A.java",
				"package p1;\n"+
				"public sealed class A extends X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y{\n" +
			"	                              ^\n" +
			"Permitted class Y does not declare p1.X as direct super class\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 5)\n" +
			"	class Z extends X{}\n" +
			"	                ^\n" +
			"The class Z with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"3. ERROR in p1\\X.java (at line 5)\n" +
			"	class Z extends X{}\n" +
			"	                ^\n" +
			"The type Z extending a sealed class X should be a permitted subtype of X\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p1\\A.java (at line 2)\n" +
			"	public sealed class A extends X{}\n" +
			"	                    ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares A as its direct superclass\n" +
			"----------\n" +
			"2. ERROR in p1\\A.java (at line 2)\n" +
			"	public sealed class A extends X{}\n" +
			"	                              ^\n" +
			"The type A extending a sealed class X should be a permitted subtype of X\n" +
			"----------\n");
	}
	public void testBug563806_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed interface X permits Y, Z{\n"+
				"}\n"+
				"class Y implements X{}\n"+
				"class Z {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed interface X permits Y, Z{\n" +
			"	                                     ^\n" +
			"Permitted type Z does not declare X as direct super interface \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	class Y implements X{}\n" +
			"	                   ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_004() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, Z, Q{\n"+
				"}\n"+
				"class Y implements X{}\n" +
				"interface Z {}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, Z, Q{\n" +
			"	                                     ^\n" +
			"Permitted type Z does not declare p1.X as direct super interface \n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, Z, Q{\n" +
			"	                                        ^\n" +
			"Q cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in p1\\X.java (at line 4)\n" +
			"	class Y implements X{}\n" +
			"	                   ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits Y, Y{\n"+
				"}\n"+
				"class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits Y, Y{\n" +
			"	                                 ^\n" +
			"Duplicate type Y for the type X in the permits clause\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	class Y extends X {}\n" +
			"	                ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_006() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p1.Y{\n"+
				"}\n"+
				"class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p1.Y{\n" +
			"	                                 ^^^^\n" +
			"Duplicate type Y for the type X in the permits clause\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 4)\n" +
			"	class Y extends X {}\n" +
			"	                ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"}\n"+
				"non-sealed class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	non-sealed class Y extends X {}\n" +
			"	                 ^\n" +
			"A class Y declared as non-sealed should have either a sealed direct superclass or a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_008() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y {\n"+
				"}\n"+
				"class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"non-sealed public interface Y {}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	class Y implements X{}\n" +
			"	                   ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	non-sealed public interface Y {}\n" +
			"	                            ^\n" +
			"An interface Y declared as non-sealed should have a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"final class Y extends X {}",
			},
			"100");
	}
	public void testBug563806_010() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	public final class Y extends p1.X{}\n" +
			"	                             ^^^^\n" +
			"The type Y extending a sealed class X should be a permitted subtype of X\n" +
			"----------\n");
	}
	public void testBug563806_011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) { \n" +
				"    System.out.println(\"0\");\n" +
				"  }\n" +
				"}\n" +
				"sealed interface Y {\n"+
				"}\n"+
				"final class Z implements Y {}",
			},
			"0");
	}
	public void testBug563806_012() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	public final class Y implements p1.X{}\n" +
			"	                                ^^^^\n" +
			"The type Y extending a sealed class X should be a permitted subtype of X\n" +
			"----------\n");
	}
	public void testBug563806_013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed interface X {\n"+
				"}\n"+
				"interface Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	interface Y extends X {}\n" +
			"	                    ^\n" +
			"The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_014() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y {\n"+
				"}\n"+
				"interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public interface Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	interface Y extends X{}\n" +
			"	                    ^\n" +
			"The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	public interface Y extends p1.X{}\n" +
			"	                           ^^^^\n" +
			"The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed\n" +
			"----------\n" +
			"2. ERROR in p2\\Y.java (at line 2)\n" +
			"	public interface Y extends p1.X{}\n" +
			"	                           ^^^^\n" +
			"The type Y extending a sealed interface X should be a permitted subtype of X\n" +
			"----------\n");
	}
	public void testBug563806_015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X permits Y{\n"+
				"}\n"+
				"final class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X permits Y{\n" +
			"	             ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_016() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public class X permits Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public class X permits Y {\n" +
			"	             ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public interface X permits Y{\n"+
				"}\n"+
				"final class Y implements X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public interface X permits Y{\n" +
			"	                 ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_018() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public interface X permits Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public interface X permits Y {\n" +
			"	                 ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_019() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p2.Y {\n" +
			"	                                 ^^^^\n" +
			"Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X\n" +
			"----------\n");
	}
	public void testBug563806_020() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X\n" +
			"----------\n");
	}
	public void testBug563806_021() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"non-sealed interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public non-sealed interface Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X\n" +
			"----------\n");
	}
	public void testBug563806_022() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"requires mod.two;\n"+
				"}\n",
				"mod.two/module-info.java",
				"module mod.two {\n" +
				"exports p2;\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y {}",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p2.Y {\n" +
			"	                                 ^^^^\n" +
			"Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p2.Y {\n" +
			"	                                 ^^^^\n" +
			"Permitted class Y does not declare p1.X as direct super class\n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testBug563806_023() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"requires mod.two;\n"+
				"}\n",
				"mod.two/module-info.java",
				"module mod.two {\n" +
				"exports p2;\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y {}",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y does not declare p1.X as direct super interface \n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testBug563806_024() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"requires mod.two;\n"+
				"}\n",
				"mod.two/module-info.java",
				"module mod.two {\n" +
				"exports p2;\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"non-sealed interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public interface Y {}",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y does not declare p1.X as direct super interface \n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testBug563806_025() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.one", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			};
		runner.runConformTest();
	}
	public void testBug563806_026() {
		associateToModule("mod.one", "p1/X.java", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			};
		runner.runConformTest();
	}
	public void testBug563806_027() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.one", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"non-sealed interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public non-sealed interface Y extends p1.X {}",
			};
		runner.runConformTest();
	}
	public void testBug563806_028() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public non-sealed enum X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public non-sealed enum X {\n" +
			"	                       ^\n" +
			"Illegal modifier for the enum X; only public is permitted\n" +
			"----------\n");
	}
	public void testBug563806_029() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed enum X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed enum X {\n" +
			"	                   ^\n" +
			"Illegal modifier for the enum X; only public is permitted\n" +
			"----------\n");
	}
	public void testBug563806_030() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public class X {\n"+
				"static sealed enum Y {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 3)\n" +
			"	static sealed enum Y {}\n" +
			"	                   ^\n" +
			"Illegal modifier for the member enum Y; only public, protected, private & static are permitted\n" +
			"----------\n");
	}
	public void testBug563806_031() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public class X {\n"+
				"static non-sealed enum Y {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 3)\n" +
			"	static non-sealed enum Y {}\n" +
			"	                       ^\n" +
			"Illegal modifier for the member enum Y; only public, protected, private & static are permitted\n" +
			"----------\n");
	}
	public void testBug563806_032() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed non-sealed interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed non-sealed interface X {\n" +
			"	                                   ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares X as its direct superclass\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed non-sealed interface X {\n" +
			"	                                   ^\n" +
			"An interface X is declared both sealed and non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_033() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed  @interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed  @interface X {\n" +
			"	       ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug563806_034() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  non-sealed @interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public  non-sealed @interface X {\n" +
			"	                              ^\n" +
			"An interface X declared as non-sealed should have a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_035() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  non-sealed interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public  non-sealed interface X {\n" +
			"	                             ^\n" +
			"An interface X declared as non-sealed should have a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_036() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    sealed class Y{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	sealed class Y{}\n" +
			"	             ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 4)\n" +
			"	sealed class Y{}\n" +
			"	             ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares Y as its direct superclass\n" +
			"----------\n");
	}
	public void testBug563806_037() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    non-sealed class Y{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	non-sealed class Y{}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n");
	}
	public void testBug563806_038() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    non-sealed sealed class Y{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	non-sealed sealed class Y{}\n" +
			"	                        ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 4)\n" +
			"	non-sealed sealed class Y{}\n" +
			"	                        ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares Y as its direct superclass\n" +
			"----------\n");
	}
	public void testBug563806_039() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"sealed class A{}\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    class Y extends A{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	sealed class A{}\n" +
			"	             ^\n" +
			"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares A as its direct superclass\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 5)\n" +
			"	class Y extends A{}\n" +
			"	                ^\n" +
			"A local class Y cannot have a sealed direct superclass or a sealed direct superinterface A\n" +
			"----------\n");
	}
	public void testBug564191_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"sealed class X permits Y, Z{\n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"final class Y extends X{}\n" +
				"final class Z extends X{}\n",
			},
			"0");
		String expectedOutput =
				"PermittedSubclasses:\n" +
				"   #33 p1/Y,\n" +
				"   #35 p1/Z\n" +
				"}";
		verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted (top-level) types make it to the .class file
	public void testBug564190_1() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n" +
					"final class Y extends X{}\n" +
					"final class Z extends X{}\n",
				},
				"0");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #33 p1/Y,\n" +
					"   #35 p1/Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted final (member) types make it to the .class file
	public void testBug564190_2() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"  final class Y extends X{}\n" +
					"  final class Z extends X{}\n" +
					"}",
				},
				"0");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #33 p1/X$Y,\n" +
					"   #36 p1/X$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted non-sealed (member) types make it to the .class file
	public void testBug564190_3() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"  non-sealed class Y extends X{}\n" +
					"  non-sealed class Z extends X{}\n" +
					"}",
				},
				"0");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #33 p1/X$Y,\n" +
					"   #36 p1/X$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted member type is reported without final, sealed or non-sealed
	public void testBug564190_4() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X  {\n" +
					"	class Y extends X {}\n" +
					"	final class Z extends Y {}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 3)\n" +
				"	class Y extends X {}\n" +
				"	                ^\n" +
				"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
				"----------\n");
	}
	// Test that implicit permitted member type with implicit permitted types
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_5() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"	sealed class Y extends X {}\n" +
					"	final class Z {}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 3)\n" +
				"	sealed class Y extends X {}\n" +
				"	             ^\n" +
				"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares Y as its direct superclass\n" +
				"----------\n");
	}
	// Test that implicit permitted member type with explicit permits clause
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_6() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X  {\n" +
					"	sealed class Y extends X permits Z {}\n" +
					"	final class Z {}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 3)\n" +
				"	sealed class Y extends X permits Z {}\n" +
				"	                                 ^\n" +
				"Permitted class Z does not declare p1.X.Y as direct super class\n" +
				"----------\n");
	}
	// Test that implicit permitted member type with explicit permits clause
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_7() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed interface SI {}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 2)\n" +
				"	sealed interface SI {}\n" +
				"	                 ^^\n" +
				"Sealed class lacks the permits clause and no top level or nested class from the same compilation unit declares SI as its direct superclass\n" +
				"----------\n");
	}
	public void testBug564450_001() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X permits Y{\n" +
					"}",
					"p1/Y.java",
					"package p1;\n"+
					"class Y extends X {\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\Y.java (at line 2)\n" +
				"	class Y extends X {\n" +
				"	                ^\n" +
				"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
				"----------\n");
	}
	public void testBug564047_001() throws CoreException, IOException {
		String outputDirectory = Util.getOutputDirectory();
		String lib1Path = outputDirectory + File.separator + "lib1.jar";
		try {
		Util.createJar(
				new String[] {
					"p/Y.java",
					"package p;\n" +
					"public sealed class Y permits Z{}",
					"p/Z.java",
					"package p;\n" +
					"public final class Z extends Y{}",
				},
				lib1Path,
				JavaCore.VERSION_15,
				true);
		String[] libs = getDefaultClassPaths();
		int len = libs.length;
		System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
		libs[len] = lib1Path;
		this.runNegativeTest(
				new String[] {
					"src/p/X.java",
					"package p;\n" +
					"public class X extends Y {\n" +
					"  public static void main(String[] args){\n" +
					"     System.out.println(0);\n" +
					"  }\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in src\\p\\X.java (at line 2)\n" +
				"	public class X extends Y {\n" +
				"	                       ^\n" +
				"The class X with a sealed direct superclass or a sealed direct superinterface Y should be declared either final, sealed, or non-sealed\n" +
				"----------\n" +
				"2. ERROR in src\\p\\X.java (at line 2)\n" +
				"	public class X extends Y {\n" +
				"	                       ^\n" +
				"The type X extending a sealed class Y should be a permitted subtype of Y\n" +
				"----------\n",
				libs,
		        true);
		} catch (IOException e) {
			System.err.println("could not write to current working directory ");
		} finally {
			new File(lib1Path).delete();
		}

	}
	public void testBug564492_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     new Y(){};\n" +
				"  }\n"+
				"}\n"+
				"sealed class Y{}\n"+
				"final class Z extends Y {\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new Y(){};\n" +
			"	    ^\n" +
			"An anonymous class cannot subclass a sealed type Y\n" +
			"----------\n");
	}
	public void testBug564492_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X  {\n"+
				"   public static void main(String[] args) {\n"+
				"        IY y = new IY(){};\n"+
				"   }\n"+
				"}\n"+
				"sealed interface I {}\n"+
				"sealed interface IY extends I {}\n"+
				"final class Z implements IY{}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	IY y = new IY(){};\n" +
			"	           ^^\n" +
			"A local class new IY(){} cannot have a sealed direct superclass or a sealed direct superinterface IY\n" +
			"----------\n");
	}
	public void testBug564492_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits A.Y {\n"+
				"       public static void main(String[] args) {\n"+
				"               new A.Y() {};\n"+
				"       }\n"+
				"}\n"+
				" \n"+
				"class A {\n"+
				"       static sealed class Y extends X permits Z {}\n"+
				"       final class Z extends Y{}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new A.Y() {};\n" +
			"	    ^^^\n" +
			"An anonymous class cannot subclass a sealed type A.Y\n" +
			"----------\n");
	}
	public void testBug564492_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public  class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               new A.IY() {};\n"+
				"       }\n"+
				"}\n"+
				" \n"+
				"class A {\n"+
				"       sealed interface I permits IY{}\n"+
				"       sealed interface IY extends I permits Z {}\n"+
				"       final class Z implements IY{}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new A.IY() {};\n" +
			"	    ^^^^\n" +
			"A local class new IY(){} cannot have a sealed direct superclass or a sealed direct superinterface A.IY\n" +
			"----------\n");
	}
	public void testBug564498_1() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"	final class Z extends Y {}\n" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #27 p1/A$Y$SubInnerY,\n" +
					"   #32 p1/A$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
			expectedOutput =
					"PermittedSubclasses:\n" +
					"   #21 p1/A$Y\n";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_2() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X {} \n" +
					"	final class Z extends Y {}\n" +
					"   final class SubY extends Y {}" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #27 p1/A$SubY,\n" +
					"   #29 p1/A$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
			expectedOutput =
					"PermittedSubclasses:\n" +
					"   #21 p1/A$Y\n";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_3() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"	final class Z extends Y {}\n" +
					"   final class SubY extends Y {}" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #27 p1/A$Y$SubInnerY,\n" +
					"   #32 p1/A$SubY,\n" +
					"   #34 p1/A$Z\n";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_4() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X permits Y.SubInnerY {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #27 p1/A$Y$SubInnerY\n";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Reject references of membertype without qualifier of enclosing type in permits clause
	public void testBug564498_5() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X permits SubInnerY {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 6)\n" +
				"	sealed class Y extends X permits SubInnerY {\n" +
				"	                                 ^^^^^^^^^\n" +
				"SubInnerY cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in p1\\X.java (at line 7)\n" +
				"	final class SubInnerY extends Y {}\n" +
				"	                              ^\n" +
				"The type SubInnerY extending a sealed class A.Y should be a permitted subtype of A.Y\n" +
				"----------\n");
	}
	// accept references of membertype without qualifier of enclosing type in permits clause
	// provided it is imported
	public void testBug564498_6() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"p1/X.java",
						"package p1;\n"+
						"import p1.Y.Z;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {}\n" +
						"}\n" +
						"sealed class Y permits Z {\n" +
						"	final class Z extends Y {}\n" +
						"}",
				},
				"");
	}
}