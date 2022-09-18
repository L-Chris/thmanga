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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class BAOZIMH extends MangaParser {

    public static final int TYPE = 103;
    public static final String DEFAULT_TITLE = "包子漫画";

    public BAOZIMH(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = StringUtils.format("https://cn.baozimh.com/search?q=%s", keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.pure-g.classify-items > div")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("a:eq(0)", "href");
                String title = node.attr("a:eq(0)", "title");
                String cover = node.attr("a:eq(0) > amp-img:eq(0)", "src");
                String author = node.text("small");
                String update = "";
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("https://cn.baozimh.com/%s", cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("cn.baozimh.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("https://cn.baozimh.com/%s", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("h1.comics-detail__title");
        String cover = body.src("div.l-content > div > div > amp-img");
        String update = body.text("div.comics-detail__info > div.supporting-text.mt-2 > div:eq(1) > span > em");
        String author = body.text("h2.comics-detail__author");
        String intro = body.text("div.comics-detail__info > p");
        boolean status = false;
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("div#chapter-items > div")) {
            String title = node.text("span");
            String path = "https://cn.baozimh.com".concat(node.attr("a", "href"));
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    private String _cid, _path;

    @Override
    public Request getImagesRequest(String cid, String path) {
        _cid = cid;
        _path = path;
        return new Request.Builder().url(path).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        Node body = new Node(html);
        int i = 0;
        for (Node node : body.list(".comic-contain amp-img")) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            String url = node.attr("src");
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
