# Watch Hystrix on Dashboard using URL, but please replace 192.168.0.21 with instance IP address:
# http://localhost:32770/hystrix-dashboard/monitor/monitor.html?streams=%5B%7B%22name%22%3A%22Tickets%22%2C%22stream%22%3A%22http%3A%2F%2F192.168.0.21%3A9090%2Factuator%2Fhystrix.stream%22%2C%22auth%22%3A%22%22%2C%22delay%22%3A%22%22%7D%5D
ab -c 10 -n 1000 http://localhost:9090/tickets