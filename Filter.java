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

--------------------------------------------------------
现在讲一种Filter的应用方式，有如下两个场景：
1、开店宝项目中，需要向外部接口请求或者用户pin和customerKey等需要。但不可能在每个controller中都重新调用接口获取一遍，所以需要用到Filter，对HttpServletRequest进行包装。
2、前端页面输入可能会带来带来一些不友好的字符，产生一些副作用如XSS攻击、SQL注入等，这个时候同样需要用Filter，对HttpServletRequest进行包装，即在getParameter的时候进行过滤。

具体的实施步骤：
1、web.xml中配置Filter标签
2、创建一个XXFilter继承Filter
3、创建一个XXRequestWrapper继承HttpServletRequestWrapper
4、如下贴出贴出Wrapper类的实现，重点是要重写getParameter方法，因为对于第一个场景，SpringMVC能够自动将参数组装到相应变量里面的原因就是httprequest.getParameter中有这个变量名称；对于第二个场景，只要在Controller中request.getParameter是经过过滤处理的就可以了，所以看代码如下：
