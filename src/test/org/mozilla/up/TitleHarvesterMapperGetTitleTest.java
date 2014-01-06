package org.mozilla.up;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;
import org.mozilla.up.TitleHarvester.ParseStats;
import org.mozilla.up.TitleHarvester.TitleHarvestMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: oyiptong
 * Date: 10/29/2013
 * Time: 12:17 PM
 */
public class TitleHarvesterMapperGetTitleTest
{
    @Mocked Mapper.Context mockedContext;
    @Mocked Counter mockCounter;
    TitleHarvester.TitleHarvestMapper thm;
    Method method;

    @BeforeMethod
    public void setUp() throws Exception
    {
        thm = new TitleHarvester.TitleHarvestMapper();
        method = TitleHarvester.TitleHarvestMapper.class.getDeclaredMethod("getTitleKeywords", String.class, Mapper.Context.class);
        method.setAccessible(true);
    }

    @Test
    public void testMapperGetTitleKeywordsEmptyInput() throws Exception
    {
        new NonStrictExpectations()
        {{
                mockedContext.getCounter(TitleHarvester.ParseStats.DATA_NO_JSON); result = mockCounter;
                mockedContext.getCounter(TitleHarvester.PageHTTPStatus.UNDEFINED); result = mockCounter;
                mockedContext.getCounter(TitleHarvester.PageHTTPStatus.REDIRECTION_3XX); result = mockCounter;
                mockCounter.increment(anyInt);
        }};

        String[] titleKeywords;

        titleKeywords = (String[]) method.invoke(thm, null, mockedContext);
        assertEquals(null, titleKeywords);

        titleKeywords = (String[]) method.invoke(thm, "", mockedContext);
        assertEquals(null, titleKeywords);

        titleKeywords = (String[]) method.invoke(thm, " ", mockedContext);
        assertEquals(null, titleKeywords);

        titleKeywords = (String[]) method.invoke(thm, "{}", mockedContext);
        assertEquals(null, titleKeywords);

        titleKeywords = (String[]) method.invoke(thm, "{\"http_result\":302}", mockedContext);
        assertEquals(null, titleKeywords);
    }

    @Test
    public void testMapperGetTitleKeywordsNoTitle() throws Exception
    {
        new NonStrictExpectations() {{
            mockedContext.getCounter(TitleHarvester.PageHTTPStatus.SUCCESS_2XX); result = mockCounter;
            mockedContext.getCounter("DATA_PAGE_TYPE", "undefined"); result = mockCounter;
            mockedContext.getCounter(TitleHarvester.ParseStats.PAGE_NO_TITLE); result = mockCounter;
            mockCounter.increment(anyInt);
        }};

        String[] inputs = new String[]
        {
                "{\"http_result\":200}",
                "{\"http_result\":200, \"content\": {\"title\": \"\"}}",
                "{\"http_result\":200, \"content\": {\"title\": \" \"}}"
        };

        String[] titleKeywords;
        for (String json : inputs)
        {
            titleKeywords = (String[]) method.invoke(thm, json, mockedContext);
            assertEquals(null, titleKeywords);
        }
    }

    @Test
    public void testMapperGetTitleKeywordsNoKeywords() throws Exception
    {
        new NonStrictExpectations() {{
            mockedContext.getCounter(TitleHarvester.PageHTTPStatus.SUCCESS_2XX); result = mockCounter;
            mockedContext.getCounter("DATA_PAGE_TYPE", "undefined"); result = mockCounter;
            mockedContext.getCounter("DATA_PAGE_TYPE", "html-doc"); result = mockCounter;
            mockedContext.getCounter(TitleHarvester.ParseStats.PAGE_NO_KEYWORDS); result = mockCounter;
            mockCounter.increment(anyInt);
        }};

        String json = "{\"http_result\":200, \"content\": {\"title\": \"AwesomeTitle\", \"type\": \"html-doc\"}}";
        String[] titleKeywords = (String[]) method.invoke(thm, json, mockedContext);
        assertEquals("AwesomeTitle", titleKeywords[0]);
        assertEquals(null, titleKeywords[1]);
    }

    @Test
    public void testMapperGetTitleKeywords() throws Exception
    {
        new NonStrictExpectations() {{
            mockedContext.getCounter(TitleHarvester.PageHTTPStatus.SUCCESS_2XX); result = mockCounter;
            mockedContext.getCounter("DATA_PAGE_TYPE", "undefined"); result = mockCounter;
            mockedContext.getCounter("DATA_PAGE_TYPE", "html-doc"); result = mockCounter;
            mockedContext.getCounter(TitleHarvester.ParseStats.PAGE_NO_KEYWORDS); result = mockCounter;
            mockCounter.increment(anyInt);
        }};

        String json = "{\"http_result\":200, \"content\": {\"title\": \"AwesomeTitle\", \"type\": \"html-doc\", \"meta_tags\": [{\"name\": \"keywords\", \"content\": \"AwesomeKeywords\"}]}}";
        String[] titleKeywords = (String[]) method.invoke(thm, json, mockedContext);
        assertEquals("AwesomeTitle", titleKeywords[0]);
        assertEquals("AwesomeKeywords", titleKeywords[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDomainCategories() throws Exception {
        new NonStrictExpectations() {{
            mockedContext.getCounter(ParseStats.ERR_URI_PARSE); result = mockCounter;
            mockCounter.increment(anyInt);
        }};

        Method buildCategories = TitleHarvester.TitleHarvestMapper.class.getDeclaredMethod("buildCategories", InputStream.class);
        buildCategories.setAccessible(true);

        String jsonString = "{\"domain1.com\":[\"category1\"],\"domain2.com\":[\"category2\"],\"domain2.com/path\":[\"category3\"]}";
        buildCategories.invoke(thm, new ByteArrayInputStream(jsonString.getBytes("UTF-8")));

        Method getCategories = TitleHarvester.TitleHarvestMapper.class.getDeclaredMethod("getCategories", String.class, Mapper.Context.class);
        getCategories.setAccessible(true);

        ArrayList<String> categories = (ArrayList<String>)getCategories.invoke(thm, "http://www.domain2.com/path/test.php", mockedContext);
        assertEquals(2, categories.size());
        assertEquals("category2", categories.get(0));
        assertEquals("category3", categories.get(1));

        categories = (ArrayList<String>)getCategories.invoke(thm, "http://subdomain.domain2.com/path/test.php", mockedContext);
        assertEquals(null, categories);

        categories = (ArrayList<String>)getCategories.invoke(thm, "http://fakedomain2.com/path/test.php", mockedContext);
        assertEquals(null, categories);

        categories = (ArrayList<String>)getCategories.invoke(thm, "http://www.domain1.com/hello.html", mockedContext);
        assertEquals(1, categories.size());
        assertEquals("category1", categories.get(0));

        categories = (ArrayList<String>)getCategories.invoke(thm, "http://subdomain.domain1.com/hello.html", mockedContext);
        assertEquals(null, categories);

        categories = (ArrayList<String>)getCategories.invoke(thm, "http://fakedomain1.com/hello.html", mockedContext);
        assertEquals(null, categories);

        categories = (ArrayList<String>)getCategories.invoke(thm, "http://www.domainother.com/hello.html", mockedContext);
        assertEquals(null, categories);
    }
}
