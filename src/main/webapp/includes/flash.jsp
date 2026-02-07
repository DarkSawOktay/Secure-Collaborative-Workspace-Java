<%
  String flash = (String) session.getAttribute("flash");
  String flashType = (String) session.getAttribute("flashType");

  if (flash != null && flashType != null) {
    String bgColor = "bg-blue-100";
    String borderColor = "border-blue-400";
    String textColor = "text-blue-700";

    if ("error".equals(flashType)) {
      bgColor = "bg-red-100";
      borderColor = "border-red-400";
      textColor = "text-red-700";
    } else if ("success".equals(flashType)) {
      bgColor = "bg-green-100";
      borderColor = "border-green-400";
      textColor = "text-green-700";
    } else if ("warning".equals(flashType)) {
      bgColor = "bg-yellow-100";
      borderColor = "border-yellow-400";
      textColor = "text-yellow-700";
    }
%>
<div class="flash-message <%= bgColor %> border <%= borderColor %> <%= textColor %> px-4 py-3 rounded mb-4">
  <%= flash %>
</div>
<%
    session.removeAttribute("flash");
    session.removeAttribute("flashType");
  }
%>
