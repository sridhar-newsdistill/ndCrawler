package com.kohlschutter.boilerpipe.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.document.TextBlock;
import com.kohlschutter.boilerpipe.extractors.ArticleExtractor;
import com.kohlschutter.boilerpipe.extractors.CommonExtractors;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;

public class NdBoilerPipe {

	static String regExToFindStartOfTheString = "^<[^>/]*>";
	static String regExForEndOfTheTag = "^</[^>]*>";
	static String delimiterBeforeOpenAndClosingTags = "<";
	static Pattern patternForStartTag = Pattern
			.compile(regExToFindStartOfTheString);
	static Pattern patternForEndTag = Pattern.compile(regExForEndOfTheTag);
	static Pattern patternForBeginningHtmlElement = Pattern
			.compile(delimiterBeforeOpenAndClosingTags);

	public static void main(String args[]) throws IOException, SAXException,
			BoilerpipeProcessingException {
		String bep = null;
		String regexForTitleTextRemoval = "<(?i)[a-zA-Z][a-zA-Z0-9]*[\\s]*(=\"(head|title))?[^>]*>[^<]{1,40}";
		Pattern patternForTitleRemoval = Pattern
				.compile(regexForTitleTextRemoval);
		String regexForAnchorTagRemoval = "<(a|A|(?i)(meta))[\\s]*[^>]*>[^<]*<\\/(a|A|(?i)(meta))[\\s]*>";
		String encodingForLineBreaks = "1922135";
		String regexForUnNecessaryTags = "<(INPUT|FORM|IFRAME|LABEL)[\\s]*[^>]*>(<\\/(IFAME|FORM|LABEL)>)?";
		String regexForRemovalOfEmptyTags = "(<(?!\\/)[^>]+>)+(<\\/[^>]+>)+";
		/*
		 * Pattern patternForEmptyTagsRemoval = Pattern
		 * .compile(regexForRemovalOfEmptyTags);
		 */// String
			// regexForUnNecessaryTags="<((INPUT)|(FORM)[\\s]*[^>](/)?>[^<]*(<\\/((INPUT)|(FORM)[\\s]*[^>]*>))?";
			// String regexForMetaTagRemoval="<?(i)(meta)>";
		String regExForUnnecessaryString = "(?i)(related[\\s]*|read[\\s]?(more|also))[\\s]:";
		String regExToFindStartOfTheString = "<[^>/]*>";
		String regExForEndOfTheTag = "</[^>]*>";
		Pattern patternForStartTag = Pattern
				.compile(regExToFindStartOfTheString);
		Pattern patternForEndTag = Pattern.compile(regExForEndOfTheTag);
		Pattern patternForUnnecessaryTagRemoval = Pattern
				.compile(regexForUnNecessaryTags);
		Pattern patternForAnchorRemoval = Pattern
				.compile(regexForAnchorTagRemoval);
		ArticleExtractor ce = null;
		List<TextBlock> contentblocks = null;
		URL pagelink = new URL("http://navbharattimes.indiatimes.com/sports/cricket/cricket-news/pain-of-not-getting-farewell-game-shall-always-remain-says-sehwag/articleshow/49609265.cms");
		InputSource ins = null;
		ce = CommonExtractors.ARTICLE_EXTRACTOR;

		final HTMLHighlighter obj = HTMLHighlighter.newHighlightingInstance();

		PrintWriter out = new PrintWriter("/home/ndindialap3/myhtml.html",
				"UTF-8");
		String result = obj.process(pagelink, ce);
		//System.out.println(obj.processHtml(pagelink, ce));
		System.out.println(result);
		result = result.replaceAll("<BR>", "");
		result = result.replaceAll("</BR>", encodingForLineBreaks);
		/*
		 * Matcher matherForEmptyTagRemoval = patternForEmptyTagsRemoval
		 * .matcher(result); result = matherForEmptyTagRemoval.replaceAll("");
		 */
		// System.out.println(result);
		Matcher mathcerForUnnecssaryTagRemoval = patternForUnnecessaryTagRemoval
				.matcher(result);

		result = mathcerForUnnecssaryTagRemoval.replaceAll("");
		// result="";
		result = "<html><head></head><body>" + result + "</body></html>";
		result = result.replaceAll("<BR>", "");
		result = result.replaceAll("</BR>", encodingForLineBreaks);
		htmlContentStack(result);
		// System.out.println(result);
		String stringForConteInfo = result.replaceAll("<(?i)(img)[^>]*>", "");

		// System.out.println(doc.toString());

		Matcher mathcerForAnchor = patternForAnchorRemoval.matcher(result);
		if (mathcerForAnchor.find()) {
			result = mathcerForAnchor.replaceAll("");
		}

	}

	public static void print(Node node, String indent) {
		Node child = node.getFirstChild();
		while (child != null) {
			print(child, indent + "");
			child = child.getNextSibling();
		}
	}

	public static void removeExtraneousContents(Elements elems) {

		if (elems.size() == 0) {
		}

		for (int i = 0; i < elems.size(); i++) {

			Element element = elems.get(i);
			if (element.children().size() == 0) {
				String value = element.text();

				StringTokenizer tokens = new StringTokenizer(value, " ");

				if (StringUtils.isEmpty(value)) {
					element.remove();

				}

				else if (tokens.countTokens() < 20) {
					element.remove();
				}
				// System.out.println(elems.toString());
			} else {
				removeExtraneousContents(element.children());
			}

		}

	}

	// we are assuming that we will get well balanced tags
	public static String htmlContentStack(String htmlString) {
		Map<String, String> numberedTagWithContent = new LinkedHashMap<String, String>();
		//Map<String, Integer> numberedTagWithTokenCount = new LinkedHashMap<String, Integer>();
		List<String> tagWithWordCount = new ArrayList<String>();
		NdContentStack stack = new NdContentStack();
		int lengthOfString = htmlString.length();
		int absposition = 0;
		int begindex = 0;
		// int endIndex=0;
		int tagnumber = 0;
		String tagName = null;
		Matcher matcherForStart = patternForStartTag.matcher(htmlString);

		while (absposition < lengthOfString - 4) {
			/*
			 * int maxBoundary = Math.min(htmlString.substring(absposition)
			 * .length(), 50);
			 */
			String startString = htmlString;
			System.out.println(htmlString);
			matcherForStart = patternForStartTag.matcher(htmlString);
			Matcher mathcerForEnd = patternForEndTag.matcher(htmlString);
			if (isBeginningOfTag(startString)) {
				if (matcherForStart.find()) {
					int position = begindex = matcherForStart.start();

					int endIndex = matcherForStart.end();
					tagName = htmlString.substring(position, endIndex);
					tagnumber++;
					htmlString = htmlString.substring(endIndex);
					absposition = absposition + tagName.length();
					// tagName = tagName + ":" + tagnumber;
				}
				stack.push(tagName + ":" + tagnumber);

			}
			if (isEndofTheTag(htmlString)) {
				if (mathcerForEnd.find()) {

					int position = begindex = mathcerForEnd.start();

					int endIndex = mathcerForEnd.end();
					tagName = htmlString.substring(position, endIndex);
					absposition += tagName.length();
					htmlString = htmlString.substring(endIndex);
				}
				int pos = stack.getTop();
				String lastTag = stack.getTagInfo().get(pos);
				if (tagName.substring(2, tagName.length() - 1) == lastTag
						.substring(1, lastTag.length() - 1))
					stack.pop();
			} else {
				if (isBeginningOfTag(htmlString)) {
					continue;
				}
				if (tagName.startsWith("</")) {
					tagName = "<p>";
				}
				Matcher mathcerForDelimiterForOpenAndClosingElement = patternForBeginningHtmlElement
						.matcher(htmlString);
				if (mathcerForDelimiterForOpenAndClosingElement.find()) {
					int endIndex = mathcerForDelimiterForOpenAndClosingElement
							.start();
					String content = htmlString.substring(0, endIndex);
					int numberofWordsInContent = content.length();
					htmlString = htmlString.substring(endIndex);
					content = tagName + content + "</"
							+ tagName.substring(1, tagName.length());
					numberedTagWithContent.put(tagName + "-" + tagnumber,
							content);
					/*
					 * numberedTagWithTokenCount.put(tagName + "-" +
					 * tagnumber+":"+numberofWordsInContent,
					 * numberofWordsInContent);
					 */
					tagWithWordCount.add(tagName + "-" + tagnumber + ":"
							+ numberofWordsInContent);
					absposition += endIndex;
				}
			}
		}

		getHtmlContent(numberedTagWithContent, tagWithWordCount);

		return null;
	}

	public static String getHtmlContent(Map<String, String> tagContent,
			List<String> tagContentLenghts) {
		int indexOfMaximumContent = 0;
		int tempIndex = 0;
		String maximumlengthContent = null;
		String tagAndContentCount = null;
		String[] tagPositionDetails = null;
		int lastseentagPos = 0;
		int currentElementTagPos = 0;
		int tagposionofMaximumLength = 0;
		List<String> tagsContentoriginal = new ArrayList<String>(
				tagContentLenghts);
		// Collections.copy(tagsContentoriginal,tagContentLenghts);
		// /*tagContentLenghts.subList(0,
		// tagContentLenghts.size());*/
		TagCountComparator tg = new TagCountComparator();
		Collections.sort(tagContentLenghts, tg);
		if (tagContentLenghts.size() != 0) {
			tagAndContentCount = tagContentLenghts
					.get(tagContentLenghts.size() - 1);
			tagPositionDetails = tagAndContentCount.split(":");

			int maxcontentLength = Integer.parseInt(tagPositionDetails[1]);
			int contentLength = maxcontentLength;
			String tagNameAndItsPoistionCoordinates[] = tagPositionDetails[0]
					.split("-");
			maximumlengthContent = tagContent.get(tagPositionDetails[0]);
			lastseentagPos = tagposionofMaximumLength = Integer
					.parseInt(tagNameAndItsPoistionCoordinates[1]);
			// Iterator<String> it= tagsContentoriginal.iterator();
			for (int i = 0; i < tagContentLenghts.size(); i++) {
				String keyname = tagContentLenghts.get(i);
				if (keyname.equals(tagAndContentCount))
					break;
				else {
					indexOfMaximumContent++;
				}
			}
			tempIndex = indexOfMaximumContent;

			while (tempIndex > 0) {
				// maximumlengthContent

				tempIndex--;
				contentLength = Integer.parseInt(tagPositionDetails[1]);
				tagAndContentCount = tagsContentoriginal.get(tempIndex);
				tagPositionDetails = tagAndContentCount.split(":");
				String currentContent = tagContent.get(tagPositionDetails[0]);
				tagNameAndItsPoistionCoordinates = tagPositionDetails[0]
						.split("-");
				currentElementTagPos = Integer
						.parseInt(tagNameAndItsPoistionCoordinates[1]);
				// very Important minimum length should be Considered
				if (lastseentagPos - currentElementTagPos > 4) {

					if (lastseentagPos - currentElementTagPos > 10) {
						break;
					}

					if (contentLength < Math.ceil(maxcontentLength * .3)) {
						continue;
					}

				} else {

					if (currentContent.length() > 32) {
						if (currentContent.startsWith("<a")
								|| currentContent.startsWith("<A")) {
							if (currentContent.length() > 150) {
								currentContent = currentContent.replaceFirst(
										"<A", "<p");
								currentContent = currentContent.replaceFirst(
										"</A", "</p");
							} else {
								currentContent = "";
							}
						}
						maximumlengthContent = currentContent
								+ maximumlengthContent;
						lastseentagPos = currentElementTagPos;
					}
				}

			}
			tempIndex = indexOfMaximumContent;
			lastseentagPos = tagposionofMaximumLength;
			while (tempIndex < tagContentLenghts.size() - 1) {

				tempIndex++;

				contentLength = Integer.parseInt(tagPositionDetails[1]);
				tagAndContentCount = tagsContentoriginal.get(tempIndex);
				tagPositionDetails = tagAndContentCount.split(":");
				String currentContent = tagContent.get(tagPositionDetails[0]);
				tagNameAndItsPoistionCoordinates = tagPositionDetails[0]
						.split("-");
				currentElementTagPos = Integer
						.parseInt(tagNameAndItsPoistionCoordinates[1]);

				if (Math.abs(lastseentagPos - currentElementTagPos) > 4) {

					if (lastseentagPos - currentElementTagPos > 10) {
						break;
					}

					if (contentLength < Math.ceil(maxcontentLength * .3)) {
						continue;
					}

				} else {
					maximumlengthContent = maximumlengthContent
							+ currentContent;
					lastseentagPos = currentElementTagPos;
				}

			}

		}
		System.out.println(maximumlengthContent);

		return maximumlengthContent;
	}

	public static boolean isBeginningOfTag(String remainingString) {
		if (!StringUtils.isEmpty(remainingString)) {
			Matcher matcherForStartOfTheTag = patternForStartTag
					.matcher(remainingString);
			boolean truthval = matcherForStartOfTheTag.find();
			System.out.println(truthval);
			return truthval;
		} else {
			return false;
		}
	}

	public static boolean isEndofTheTag(String remainingString) {
		if (!StringUtils.isEmpty(remainingString)) {
			Matcher mathcerForEnd = patternForEndTag.matcher(remainingString);
			boolean truthval = mathcerForEnd.find();
			System.out.println(truthval);
			return truthval;
		} else {
			return false;
		}
		// return false;
	}

	public static boolean isContent(String remainingString, int pos) {

		return false;
	}

}
