#!/usr/bin/env bash
#!/usr/bin/env bash
docker run -p 8080:8080 -e SPORT_FAILURE=.8 -e EXHIBITION_FAILURE=.3 -e CONCERT_FAILURE=.1 io.workshop/funtastic:0.0.1-SNAPSHOT