const express = require('express');
const cors = require('cors');
const http = require('http');
const path = require('path');
const fs = require('fs');
const { createProxyMiddleware } = require('http-proxy-middleware');
const serverCfg = require('./config/server-mode.node.js');

const { getCurrentServerConfig, printConfig } = serverCfg;
const currentConfig = getCurrentServerConfig();
const port = currentConfig.port;

const app = express();

// CORS config (gateway only)
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept', 'Origin'],
  credentials: true
}));

function resolveExistingPath(candidates) {
  for (const candidate of candidates) {
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }
  return null;
}

const adminRoot = resolveExistingPath([
  path.join(__dirname, 'admin'),
  path.join(__dirname, '../Live/admin'),
  path.join(__dirname, '../frontend/admin')
]);

const staticRoot = resolveExistingPath([
  path.join(__dirname, 'static'),
  path.join(__dirname, '../Live/static'),
  path.join(__dirname, '../frontend/static')
]);

if (adminRoot) {
  app.use('/admin', express.static(adminRoot));
  app.get('/admin', (req, res) => {
    res.sendFile(path.join(adminRoot, 'index.html'));
  });
}

if (staticRoot) {
  app.use('/static', express.static(staticRoot));
}

// H5 前端静态文件 - 放在根路径
const h5Root = resolveExistingPath([
  path.join(__dirname, '../h5'),
  path.join(__dirname, 'h5')
]);

if (h5Root) {
  app.use(express.static(h5Root));
}

// Proxy config
const backendTarget = process.env.BACKEND_SERVER_URL || 'http://localhost:8000';

const apiProxy = createProxyMiddleware({
  target: backendTarget,
  changeOrigin: true,
  pathRewrite: { '^/api': '/api' }
});

const wsProxy = createProxyMiddleware({
  target: backendTarget,
  changeOrigin: true,
  ws: true
});

app.use('/api', apiProxy);
app.use('/ws', wsProxy);

app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'gateway ok',
    data: {
      backendTarget,
      adminRoot: adminRoot || null,
      staticRoot: staticRoot || null
    },
    timestamp: Date.now()
  });
});

const server = http.createServer(app);

server.on('upgrade', wsProxy.upgrade);

server.listen(port, () => {
  if (typeof printConfig === 'function') {
    printConfig();
  }
  console.log(`Gateway listening on :${port}`);
  console.log(`Proxy target: ${backendTarget}`);
  if (!adminRoot) {
    console.warn('Admin static root not found. Set admin files under live-gateway/admin or ../Live/admin.');
  }
  if (!staticRoot) {
    console.warn('Static root not found. Set static files under live-gateway/static or ../Live/static.');
  }
});
