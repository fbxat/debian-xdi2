package xdi2.webtools.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.features.nodetypes.XdiContext;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;

/**
 * Servlet implementation class for Servlet: XDIParser
 *
 */
public class XDIParser extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private static final long serialVersionUID = 1108625332231655077L;

	private static Logger log = LoggerFactory.getLogger(XDIParser.class);

	private static String sampleInput;

	static {

		InputStream inputStream = XDIParser.class.getResourceAsStream("sample.address");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int i;

		try {

			while ((i = inputStream.read()) != -1) outputStream.write(i);
			sampleInput = new String(outputStream.toByteArray());
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		} finally {

			try { inputStream.close(); } catch (Exception ex) { }
			try { outputStream.close(); } catch (Exception ex) { }
		}
	}

	public XDIParser() {

		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String sample = request.getParameter("sample");
		if (sample == null) sample = "1";

		request.setAttribute("rules", new String[0]);
		request.setAttribute("rulename", "xdi-statement");
		request.setAttribute("parser", "manual");
		request.setAttribute("input", sampleInput);

		request.getRequestDispatcher("/XDIParser.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String rulename = request.getParameter("rulename");
		String parser = request.getParameter("parser");
		String input = request.getParameter("input");
		String output1 = "";
		String output2 = "";
		String output3 = "";
		String output4 = "";
		String output5 = "";
		String output6 = "";
		String output7 = "";
		String output8 = "";
		String error = null;

		try {

//			ByteArrayOutputStream buffer1 = new ByteArrayOutputStream();
//			ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
//			ByteArrayOutputStream buffer3 = new ByteArrayOutputStream();
//			ByteArrayOutputStream buffer4 = new ByteArrayOutputStream();
			//			ByteArrayOutputStream buffer5 = new ByteArrayOutputStream();
			//			ByteArrayOutputStream buffer6 = new ByteArrayOutputStream();
			//			ByteArrayOutputStream buffer7 = new ByteArrayOutputStream();
			ByteArrayOutputStream buffer8 = new ByteArrayOutputStream();
//			PrintStream stream1 = new PrintStream(buffer1);
//			PrintStream stream2 = new PrintStream(buffer2);
//			PrintStream stream3 = new PrintStream(buffer3);
//			PrintStream stream4 = new PrintStream(buffer4);
			//			PrintStream stream5 = new PrintStream(buffer5);
			//			PrintStream stream6 = new PrintStream(buffer6);
			//			PrintStream stream7 = new PrintStream(buffer7);
			PrintStream stream8 = new PrintStream(buffer8);

			/*   DEPRECATED  if ("aparse".equals(parser)) {

				List<Deque<String>> stackDeques;
				Set<Entry<String, Integer>> countEntrySet;

				xdi2.syntax.parser.aparse.Rule rule = xdi2.core.syntax.aparse.Parser.parse(rulename, input);

				// tree

				rule.accept(new xdi2.syntax.parser.aparse.TreeDisplayer(stream1));
				output1 = html(new String(buffer1.toByteArray(), Charset.forName("UTF-8")));

				// stack

				xdi2.syntax.parser.aparse.DequesVisitor dequesVisitor = new xdi2.syntax.parser.aparse.DequesVisitor();
				rule.accept(dequesVisitor);
				stackDeques = dequesVisitor.getDeques();

				// xml

				PrintStream out = System.out;
				System.setOut(stream3);
				rule.accept(new xdi2.syntax.parser.aparse.XmlDisplayer());
				System.setOut(out);

				// count

				xdi2.syntax.parser.aparse.CountVisitor countVisitor = new xdi2.syntax.parser.aparse.CountVisitor();
				rule.accept(countVisitor);
				countEntrySet = countVisitor.getCount().entrySet();

				stream2.println("<table border='1' cellpadding='5'><tr>");
				for (Deque<String> deque : stackDeques) {

					String terminal = deque.peekLast().replace("\"", "&quot;");
					StringBuffer stack = new StringBuffer();
					String stackentry;
					while ((stackentry = deque.pollFirst()) != null) stack.append(stackentry.replace("\"", "&quot;") + "<br>");
					stream2.println("<td onmouseover=\"document.getElementById('stack').innerHTML='" + stack.toString() + "';\" style='cursor:default;font-size:13pt;font-weight:bold;'>" + terminal + "</td>");
				}
				stream2.println("</tr></table>");
				stream2.println("<div id='stack'></div>");
				output2 = new String(buffer2.toByteArray(), Charset.forName("UTF-8"));

				// xml

				output3 = html(new String(buffer3.toByteArray(), Charset.forName("UTF-8")));

				// count

				for (Entry<String, Integer> entry : countEntrySet) stream4.println(entry.getKey() + ": " + entry.getValue());
				output4 = html(new String(buffer4.toByteArray(), Charset.forName("UTF-8")));
			} /*   DEPRECATED  else if ("apg".equals(parser)) {

				com.coasttocoastresearch.apg.Grammar g;
				int r = -1;

				g = xdi2.syntax.parser.apg.XDI3Grammar.getInstance();
				for (xdi2.syntax.parser.apg.XDI3Grammar.RuleNames rule : xdi2.syntax.parser.apg.XDI3Grammar.RuleNames.values()) if (rule.ruleName().equals(rulename)) r = rule.ruleID();

				com.coasttocoastresearch.apg.Parser p = new com.coasttocoastresearch.apg.Parser(g);

				p.setStartRule(r);
				p.setInputString(input);

				Statistics statistics = p.enableStatistics(true);

				Trace trace = p.enableTrace(true);
				trace.setOut(stream7);

				Result result = p.parse();

				result.displayResult(stream5);
				output5 = html(new String(buffer5.toByteArray(), Charset.forName("UTF-8")));

				statistics.displayStats(stream6, "rules");
				output6 = html(new String(buffer6.toByteArray(), Charset.forName("UTF-8")));

				output7 = html(new String(buffer7.toByteArray(), Charset.forName("UTF-8")));
			} else*/ if ("manual".equals(parser)) {

				XDIAddress XDIaddress;

				try {

					XDIStatement statement = XDIStatement.create(input);
					XDIaddress = statement.getSubject();
				} catch (Exception ex) {

					XDIaddress = XDIAddress.create(input);
				}

				Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
				List<ContextNode> contextNodes = new ArrayList<ContextNode> ();

				for (ContextNode contextNode = tempGraph.setDeepContextNode(XDIaddress); contextNode != null; contextNode = contextNode.getContextNode()) {

					contextNodes.add(contextNode);
				}

				tempGraph.close();

				Collections.reverse(contextNodes);

				for (ContextNode contextNode : contextNodes) {

					XdiContext<?> xdiContext = XdiAbstractContext.fromContextNode(contextNode);
					Class<?> clazz = xdiContext.getClass();

					stream8.print("<b>" + contextNode.getXDIArc() + "</b>" + ": ");

					stream8.print(clazz.getCanonicalName() + " ");

					List<Class<?>> interfazes = interfazes(clazz);
					Collections.reverse(interfazes);
					interfazes = dedupe(interfazes);

					for (Class<?> interfaze : interfazes) {

						if (! interfaze.getCanonicalName().startsWith("xdi2.core.features.nodetypes.")) continue;

						stream8.print("(" + interfaze.getSimpleName() + ")");
					}

					stream8.println();
				}

				output8 = html(new String(buffer8.toByteArray(), Charset.forName("UTF-8")));
			} else {

				throw new RuntimeException("Parser not supported.");
			}
		} catch (Exception ex) {

			log.error(ex.getMessage(), ex);
			error = ex.getMessage();
			if (error == null) error = ex.getClass().getName();
		}

		// display results

		request.setAttribute("rules", new String[0]);
		request.setAttribute("rulename", rulename);
		request.setAttribute("parser", parser);
		request.setAttribute("input", input.replace("\"", "&quot;"));
		request.setAttribute("output1", output1);
		request.setAttribute("output2", output2);
		request.setAttribute("output3", output3);
		request.setAttribute("output4", output4);
		request.setAttribute("output5", output5);
		request.setAttribute("output6", output6);
		request.setAttribute("output7", output7);
		request.setAttribute("output8", output8);
		request.setAttribute("error", error);

		request.getRequestDispatcher("/XDIParser.jsp").forward(request, response);
	}

	private static List<Class<?>> interfazes(Class<?> clazz) {

		if (clazz == null) return Collections.emptyList();
		if (! clazz.getCanonicalName().startsWith("xdi2.core.features.nodetypes.")) return Collections.emptyList();

		List<Class<?>> list = new ArrayList<Class<?>> ();

		for (Class<?> interfaze : clazz.getInterfaces()) {

			list.add(interfaze);
			list.addAll(interfazes(interfaze));
		}

		list.addAll(interfazes(clazz.getSuperclass()));

		return list;
	}

	private static List<Class<?>> dedupe(List<Class<?>> list) {

		return new ArrayList<Class<?>>(new LinkedHashSet<Class<?>> (list));
	}

	private static String html(String string) {

		return string.replace("<", "&lt;").replace(">", "&gt;").replace("&lt;b&gt;", "<b>").replace("&lt;/b&gt;", "</b>");
	}
}

