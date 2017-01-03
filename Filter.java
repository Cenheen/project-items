public class XssHttpServletRequestWrapper  extends HttpServletRequestWrapper {
    private static final Logger LOGGER = new Logger();

    public XssHttpServletRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values==null)  {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXSS(parameter, values[i]);
        }
        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        return cleanXSS(parameter, value);
    }

    private String cleanXSS(final String parameter, final String value) {
        String clearValue = value;
        clearValue = clearValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        clearValue = clearValue.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
        clearValue = clearValue.replaceAll("'", "&#39;");
        clearValue = clearValue.replaceAll("eval\\((.*)\\)", "");
        clearValue = clearValue.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        //value = value.replaceAll("script", "");
        if (!value.equals(clearValue)) {
            LOGGER.warn("#cleanXSS 输入非法字符:", parameter, " value:", value, " url:", getRequestURL(), " cookies:", getHeader(HttpHeaders.COOKIE));
        }
        return clearValue;
    }

}
5、在XXFilter里面重写doFilter方法，如下
public class SecurityFilter implements Filter {
    FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        this.filterConfig = null;
    
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);
    }
}
