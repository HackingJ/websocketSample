<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>

<P>  The time on the server is ${serverTime}. </P>
<script type="text/javascript">
  var socket, serverUrl;
  if (!window.WebSocket) {
    alert('your browser so fucking. It does not support websocket. please use modern browser');
  } else {
    serverUrl = "${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/websockets";
    serverUrl = serverUrl.replace('http', 'ws');
    socket = new WebSocket(serverUrl);
    socket.addEventListener('open', function(e) {
      alert('open!');
      socket.send("It is client message!");
    });
    socket.addEventListener('error', function(e) {
      alert('error!');
    });
    socket.addEventListener('message', function(e) {
      alert('receive! ' + e.data);
    });
    socket.addEventListener('close', function(e) {
      alert('close');
    })
  }
</script>
</body>
</html>
