import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.storage.file.FileBasedConfig;
		testDb = new TestRepository<>(db);
	@Test
	public void testDiffAutoCrlfSmallFile() throws Exception {
		String content = "01234\r\n01234\r\n01234\r\n";
		String expectedDiff = "diff --git a/test.txt b/test.txt\n"
				+ "index fe25983..a44a032 100644\n" //
				+ "--- a/test.txt\n" //
				+ "+++ b/test.txt\n" //
				+ "@@ -1,3 +1,4 @@\n" //
				+ " 01234\n" //
				+ "+ABCD\n" //
				+ " 01234\n" //
				+ " 01234\n";
		doAutoCrLfTest(content, expectedDiff);
	}

	@Test
	public void testDiffAutoCrlfMediumFile() throws Exception {
		String content = mediumCrLfString();
		String expectedDiff = "diff --git a/test.txt b/test.txt\n"
				+ "index 215c502..c10f08c 100644\n" //
				+ "--- a/test.txt\n" //
				+ "+++ b/test.txt\n" //
				+ "@@ -1,4 +1,5 @@\n" //
				+ " 01234567\n" //
				+ "+ABCD\n" //
				+ " 01234567\n" //
				+ " 01234567\n" //
				+ " 01234567\n";
		doAutoCrLfTest(content, expectedDiff);
	}

	@Test
	public void testDiffAutoCrlfLargeFile() throws Exception {
		String content = largeCrLfString();
		String expectedDiff = "diff --git a/test.txt b/test.txt\n"
				+ "index 7014942..c0487a7 100644\n" //
				+ "--- a/test.txt\n" //
				+ "+++ b/test.txt\n" //
				+ "@@ -1,4 +1,5 @@\n"
				+ " 012345678901234567890123456789012345678901234567\n"
				+ "+ABCD\n"
				+ " 012345678901234567890123456789012345678901234567\n"
				+ " 012345678901234567890123456789012345678901234567\n"
				+ " 012345678901234567890123456789012345678901234567\n";
		doAutoCrLfTest(content, expectedDiff);
	}

	private void doAutoCrLfTest(String content, String expectedDiff)
			throws Exception {
		FileBasedConfig config = db.getConfig();
		config.setString(ConfigConstants.CONFIG_CORE_SECTION, null,
				ConfigConstants.CONFIG_KEY_AUTOCRLF, "true");
		config.save();
		commitFile("test.txt", content, "master");
		// Insert a line into content
		int i = content.indexOf('\n');
		content = content.substring(0, i + 1) + "ABCD\r\n"
				+ content.substring(i + 1);
		writeTrashFile("test.txt", content);
		// Create the patch
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();
				DiffFormatter dfmt = new DiffFormatter(
						new BufferedOutputStream(os))) {
			dfmt.setRepository(db);
			dfmt.format(new DirCacheIterator(db.readDirCache()),
					new FileTreeIterator(db));
			dfmt.flush();

			String actual = os.toString("UTF-8");

			assertEquals(expectedDiff, actual);
		}
	}

	private static String largeCrLfString() {
		String line = "012345678901234567890123456789012345678901234567\r\n";
		StringBuilder builder = new StringBuilder(
				2 * RawText.FIRST_FEW_BYTES);
		while (builder.length() < 2 * RawText.FIRST_FEW_BYTES) {
			builder.append(line);
		}
		return builder.toString();
	}

	private static String mediumCrLfString() {
		// Create a CR-LF string longer than RawText.FIRST_FEW_BYTES whose
		// canonical representation is shorter than RawText.FIRST_FEW_BYTES.
		String line = "01234567\r\n"; // 10 characters
		StringBuilder builder = new StringBuilder(
				RawText.FIRST_FEW_BYTES + line.length());
		while (builder.length() <= RawText.FIRST_FEW_BYTES) {
			builder.append(line);
		}
		return builder.toString();
	}
