Here’s the corrected version of your markdown:

# Running

```bash
docker-compose up
```

This command will run the hacker server on port 8888 and the app server on port 9999.

Unfortunately, for browsers, ports on the same domain (localhost) are not treated as different origins, so it’s important to modify `/etc/hosts` (Linux, macOS) and add these lines:

```
127.0.0.1 localhost-1
127.0.0.1 localhost-2
```

This will emulate different domains, and now CSRF prevention will work:

- http://localhost-1:8888 - hacker app that tries to forge requests
- http://localhost-2:9999 - main app with basic protection

# How it Works

TODO: add description with diagrams
