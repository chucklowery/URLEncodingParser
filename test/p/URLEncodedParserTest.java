package p;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.junit.internal.matchers.IsCollectionContaining.hasItems;

public class URLEncodedParserTest {

    @Test
    public void givenMultibyteCharactersInName_expectMultibyteCharactersInKey() {
        final String NAME = "Āき";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey("Āき"), is(true));
    }

    @Test
    public void givenValueButNoKey_expectNullKey() {
        final String NAME = "=abcd";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey(null), is(true));
        assertThat(pairs.get(null), hasItem("abcd"));
    }

    @Test
    public void givenMultipleValuesWithPlusToBeReplacedBySpaces() {
        final String NAME = "a%2B+=1&a%2B+=2&a%2B+=3+++%2B";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey("a+ "), is(true));
        assertThat(pairs.get("a+ "), hasItems("1", "2", "3   +"));
    }

    @Test
    public void givenMultipleValuesForTheSameKey_expectMultipleValues() {
        final String NAME = "a=1&a=2&a=3";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey("a"), is(true));
        assertThat(pairs.get("a"), hasItems("1", "2", "3"));
    }

    @Test
    public void givenPercentEncodingUpperAndLowerCase_expectEncodedValues() {
        final String NAME = "%3D%3d=%3D%3d";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey("=="), is(true));
        assertThat(pairs.get("=="), hasItem("=="));
    }

    @Test
    public void givenNameWithSpecialCharacters_expectSpecialCharacterValue() {
        final String NAME = "%20%21%5D=%20%21%5D";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey(" !]"), is(true));
        assertThat(pairs.get(" !]"), hasItem(" !]"));
    }

    @Test
    public void givenNameEqualsNameWithExtraEquals_expectValueWithExtraEquals() {
        final String NAME = "a=b====";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey("a"), is(true));
        assertThat(pairs.get("a"), hasItem("b===="));
    }

    @Test
    public void givenNameEqualsNameAmpersandNameEqualsName_expectTwoKeyValues() {
        final String NAME = "a=b&c=d";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(2));
        assertThat(pairs.containsKey("a"), is(true));
        assertThat(pairs.containsKey("c"), is(true));
        assertThat(pairs.get("a"), hasItem("b"));
        assertThat(pairs.get("c"), hasItem("d"));
    }

    @Test
    public void givenNameEqualsName_expectKeyValue() {
        final String NAME = "a=b";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey("a"), is(true));
        assertThat(pairs.get("a"), hasItem("b"));
    }

    @Test
    public void givenOnlyAmperSand_expextNothing() {
        final String NAME = "&&&";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(0));
    }

    @Test
    public void givenSingleName_expextSingleKey() {
        final String NAME = "abcd";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey(NAME), is(true));
        assertThat(pairs.get(NAME), hasItem((String) null));
    }

    public void givenSingleNameFollowedByApersand_expectSingleKey() {
        final String NAME = "abcd&";

        Map<String, List<String>> pairs = context.parse(createStream(NAME), NAME.length());

        assertThat(pairs.size(), is(1));
        assertThat(pairs.containsKey(NAME), is(true));
        assertThat(pairs.get(NAME), hasItem((String) null));
    }

    @Test(expected = StreamInvalidException.class)
    public void givenPercentFollowedByPercent() {
        final String NAME = "%%";
        context.parse(createStream(NAME), NAME.length());
    }

    @Test(expected = StreamInvalidException.class)
    public void givenPercentFollowedByNonHexValue() {
        final String NAME = "%kka";
        context.parse(createStream(NAME), NAME.length());
    }

    @Test(expected = StreamInvalidException.class)
    public void givenPercentFollowedByUpperBoundingValue() {
        final String NAME = "%G";
        context.parse(createStream(NAME), NAME.length());
    }

    @Test(expected = StreamInvalidException.class)
    public void givenPercentFollowedByLowerValue_expectExpection() {
        final String NAME = "%/";
        context.parse(createStream(NAME), NAME.length());
    }

    @Test(expected = StreamInvalidException.class)
    public void givenPercentFollowedByPlus_expectError() {
        final String NAME = "%++";
        context.parse(createStream(NAME), NAME.length());
    }

    URLEncodedParser context;

    @Before
    public void before() {
        context = new URLEncodedParser();

    }

    private ByteArrayInputStream createStream(final String NAME) {
        ByteArrayInputStream stream = new ByteArrayInputStream(NAME.getBytes(StandardCharsets.UTF_8));
        return stream;
    }

}
