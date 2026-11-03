package com.dipsource.service.news.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** RSS/Atom 본문을 뉴스 초안으로 바꾼다. HTTP는 하지 않는다. */
@Component
public class NewsRssParser {

    static final String SOURCE = "GeekNews";
    static final int SUMMARY_LENGTH = 120;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(x?[0-9a-fA-F]+);");

    public List<NewsItemDraft> parse(String xml) {
        return parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public List<NewsItemDraft> parse(InputStream xml) {
        try (XmlReader reader = new XmlReader(xml)) {
            SyndFeedInput input = new SyndFeedInput();
            input.setAllowDoctypes(false);
            SyndFeed feed = input.build(reader);
            List<NewsItemDraft> items = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries()) {
                NewsItemDraft item = toItem(entry);
                if (item != null) {
                    items.add(item);
                }
            }
            items.sort(Comparator.comparing(NewsItemDraft::publishedAt, Comparator.nullsLast(Comparator.reverseOrder())));
            return List.copyOf(items);
        } catch (IOException | FeedException ex) {
            throw new IllegalStateException("RSS를 해석하지 못했습니다", ex);
        }
    }

    private static NewsItemDraft toItem(SyndEntry entry) {
        String title = clean(entry.getTitle());
        String url = entry.getLink();
        if (url == null || url.isBlank()) {
            url = entry.getUri();
        }
        if (title.isBlank() || url == null || url.isBlank()) {
            return null;
        }
        String summary = summary(entry);
        return new NewsItemDraft(
                title,
                summary,
                SOURCE,
                NewsCategories.classify(title, summary),
                toSeoul(published(entry)),
                url
        );
    }

    private static String summary(SyndEntry entry) {
        String description = entry.getDescription() == null ? null : entry.getDescription().getValue();
        // GeekNews는 Atom이라 description 없이 본문이 <content type="html">에 들어온다.
        if ((description == null || description.isBlank()) && !entry.getContents().isEmpty()) {
            description = entry.getContents().get(0).getValue();
        }
        return abbreviate(clean(description));
    }

    static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String text = decodeEntities(raw.replaceAll("(?s)<[^>]*>", " "));
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String decodeEntities(String text) {
        Matcher matcher = NUMERIC_ENTITY.matcher(text);
        StringBuilder decoded = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group(1);
            String replacement;
            try {
                int codePoint = token.charAt(0) == 'x' || token.charAt(0) == 'X'
                        ? Integer.parseInt(token.substring(1), 16)
                        : Integer.parseInt(token);
                replacement = Character.isValidCodePoint(codePoint)
                        ? Matcher.quoteReplacement(Character.toString(codePoint))
                        : Matcher.quoteReplacement(matcher.group());
            } catch (NumberFormatException ignored) {
                replacement = Matcher.quoteReplacement(matcher.group());
            }
            matcher.appendReplacement(decoded, replacement);
        }
        matcher.appendTail(decoded);
        return decoded.toString()
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }

    static String abbreviate(String text) {
        if (text.length() <= SUMMARY_LENGTH) {
            return text;
        }
        return text.substring(0, SUMMARY_LENGTH).trim();
    }

    private static Date published(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return entry.getPublishedDate();
        }
        return entry.getUpdatedDate();
    }

    private static OffsetDateTime toSeoul(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(SEOUL).toOffsetDateTime();
    }
}
