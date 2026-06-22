const fs = require('fs');
const path = require('path');
const cssPath = path.join(__dirname, 'src', 'index.css');
let css = fs.readFileSync(cssPath, 'utf8');

// Wrap everything from /* ── Glass Card ── */ down to the end in @layer components?
// Or maybe just the button and input styles.
