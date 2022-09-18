package com.haleydu.cimoc.source;

import com.haleydu.cimoc.model.Chapter;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.parser.MangaParser;
import com.haleydu.cimoc.parser.NodeIterator;
import com.haleydu.cimoc.parser.SearchIterator;
import com.haleydu.cimoc.parser.UrlFilter;
import com.haleydu.cimoc.soup.Node;
import com.haleydu.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class Common extends MangaParser {

    public static final int TYPE = -1;
    public static final String DEFAULT_TITLE = "包子漫画";
    public static final String baseUrl = "";
    // 搜索页字段
    public static final String search = "";
    public static final String searchInfoList = "";
    public static final String searchInfoCid = "";
    public static final String searchInfoTitle = "";
    public static final String searchInfoCover = "";
    public static final String searchInfoAuthor = "";
    public static final String searchInfoUpdate = "";
    // 详情页字段
    public static final String parseInfoTitle = "";
    public static final String parseInfoCover = "";
    public static final String parseInfoIntro = "";
    public static final String parseInfoAuthor = "";
    public static final String parseInfoUpdate = "";
    public static final String parseInfoStatus = "";
    public static final String parseChapterList1 = "";
    public static final String parseChapterList2 = "";
    public static final String parseChapterTitle = "";
    public static final String parseChapterPath = "";
    // 观看页字段
    public static final String parseImageList = "";
    public static final String parseImageUrl = "src";
    // 分类页字段
    public static final String parseCategoryInfoList = "";
    public static final String parseCategoryInfoCid = "";
    public static final String parseCategoryInfoTitle = "";
    public static final String parseCategoryInfoCover = "";
    public static final String parseCategoryInfoAuthor = "";
    public static final String parseCategoryInfoUpdate = "";
    public static final String parseCategoryPath = "";


    public Common(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = StringUtils.format(baseUrl + search, keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(searchInfoList)) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr(searchInfoCid, "href");
                String title = node.attr(searchInfoTitle, "title");
                String cover = node.attr(searchInfoCover, "src");
                String author = node.text(searchInfoAuthor);
                String update = node.text(searchInfoUpdate);
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("https://cn.baozimh.com/%s", cid);
    }

    @Override
    protected void initUrlFilterList() { }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node node = new Node(html);
        String title = node.text(parseInfoTitle);
        String cover = node.src(parseInfoCover);
        String update = node.text(parseInfoUpdate);
        String author = node.text(parseInfoAuthor);
        String intro = node.text(parseInfoIntro);
        boolean status = parseInfoStatus.length() > 0 && isFinish(node.text(parseInfoStatus));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list(parseChapterList1)) {
            String title = node.text(parseChapterTitle);
            String path = baseUrl + node.attr(parseChapterPath, "href");
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return new Request.Builder().url(path).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        Node body = new Node(html);
        int i = 0;
        for (Node node : body.list(parseImageList)) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            String url = node.attr(parseImageUrl);
            list.add(new ImageUrl(id, comicChapter, i + 1, url, false));
            i++;
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder()
//                .addHeader("Referer", url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .addHeader("Cookie", "isAdult=1")
                .url(url).build();
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public Headers getHeader(String url) {
        return Headers.of("Referer", url);
    }

}
