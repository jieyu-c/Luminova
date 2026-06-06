(function () {
  const board = document.querySelector('.canvas-board');
  const stage = document.querySelector('.canvas-stage');
  const area = document.querySelector('.canvas-area');
  if (!board || !stage) return;

  const nodes = [...board.querySelectorAll('.media-node')];
  const wires = board.querySelector('.canvas-wires');
  const railButtons = [...document.querySelectorAll('.canvas-rail .rail-btn')];

  const NODE_WIDTH = {
    'node-script': 260,
    'node-role': 260,
    'node-shot': 280,
    'node-prompt': 280,
    'node-video': 300,
    'node-scene': 300,
  };

  const EDGES = [
    ['node-script', 'out', 'node-shot', 'in'],
    ['node-role', 'out', 'node-shot', 'in'],
    ['node-shot', 'out', 'node-prompt', 'in'],
    ['node-prompt', 'out', 'node-video', 'in'],
    ['node-scene', 'out', 'node-video', 'in'],
  ];

  const SNAP = 18;
  const INITIAL = {
    'node-script': { x: 24, y: 24 },
    'node-shot': { x: 316, y: 24 },
    'node-video': { x: 628, y: 24 },
    'node-role': { x: 24, y: 340 },
    'node-prompt': { x: 316, y: 340 },
    'node-scene': { x: 628, y: 400 },
  };
  let tool = 'select';
  let drag = null;
  let pan = null;
  let wirePaths = [];

  function snap(v) {
    return Math.round(v / SNAP) * SNAP;
  }

  function nodeKey(node) {
    return [...node.classList].find((c) => c.startsWith('node-'));
  }

  function isInteractive(target) {
    return !!target.closest('button, input, textarea, select, a, label, .port');
  }

  function selectNode(node) {
    nodes.forEach((n) => n.classList.toggle('selected', n === node));
  }

  function applyNodeWidths() {
    nodes.forEach((node) => {
      const key = nodeKey(node);
      if (key && NODE_WIDTH[key]) node.style.width = NODE_WIDTH[key] + 'px';
    });
  }

  function applyInitialLayout() {
    applyNodeWidths();
    board.classList.add('layout-absolute');
    nodes.forEach((node) => {
      const pos = INITIAL[nodeKey(node)];
      if (!pos) return;
      node.style.left = pos.x + 'px';
      node.style.top = pos.y + 'px';
    });
    resizeBoard();
    updateWires();
  }

  function resizeBoard() {
    let maxR = 0;
    let maxB = 0;
    nodes.forEach((node) => {
      maxR = Math.max(maxR, node.offsetLeft + node.offsetWidth);
      maxB = Math.max(maxB, node.offsetTop + node.offsetHeight);
    });
    board.style.width = Math.max(900, maxR + 120) + 'px';
    board.style.height = Math.max(520, maxB + 120) + 'px';
    if (wires) {
      wires.setAttribute('width', board.style.width);
      wires.setAttribute('height', board.style.height);
    }
  }

  function portPoint(node, side) {
    const port = node.querySelector('.port.' + side);
    const anchor = port || node;
    const boardRect = board.getBoundingClientRect();
    const r = anchor.getBoundingClientRect();
    return {
      x: r.left + r.width / 2 - boardRect.left,
      y: r.top + r.height / 2 - boardRect.top,
    };
  }

  function wirePath(from, to) {
    const dx = to.x - from.x;
    const bend = Math.max(48, Math.abs(dx) * 0.45);
    const c1x = from.x + (dx >= 0 ? bend : -bend);
    const c2x = to.x - (dx >= 0 ? bend : -bend);
    return `M ${from.x} ${from.y} C ${c1x} ${from.y}, ${c2x} ${to.y}, ${to.x} ${to.y}`;
  }

  function ensureWirePaths() {
    if (!wires || wirePaths.length) return;
    const ns = 'http://www.w3.org/2000/svg';
    EDGES.forEach(() => {
      const path = document.createElementNS(ns, 'path');
      path.setAttribute('class', 'wire-path');
      wires.appendChild(path);
      wirePaths.push(path);
    });
  }

  function updateWires() {
    if (!wires) return;
    ensureWirePaths();
    EDGES.forEach(([fromKey, fromSide, toKey, toSide], i) => {
      const fromNode = board.querySelector('.' + fromKey);
      const toNode = board.querySelector('.' + toKey);
      const path = wirePaths[i];
      if (!fromNode || !toNode || !path) return;
      const from = portPoint(fromNode, fromSide);
      const to = portPoint(toNode, toSide);
      path.setAttribute('d', wirePath(from, to));
    });
  }

  function boardPoint(clientX, clientY) {
    const boardRect = board.getBoundingClientRect();
    return {
      x: clientX - boardRect.left,
      y: clientY - boardRect.top,
    };
  }

  function startNodeDrag(node, e) {
    if (tool !== 'select' || e.button !== 0) return;
    const zone = e.target.closest('.node-toolbar, .node-preview');
    if (!zone || !node.contains(zone)) return;
    if (e.target.closest('.node-compose, button, input, textarea, select, a, .video-controls')) return;

    e.preventDefault();
    node.setPointerCapture(e.pointerId);
    const p = boardPoint(e.clientX, e.clientY);
    const left = parseFloat(node.style.left) || 0;
    const top = parseFloat(node.style.top) || 0;

    drag = {
      node,
      pointerId: e.pointerId,
      offsetX: p.x - left,
      offsetY: p.y - top,
    };
    node.classList.add('is-dragging');
    board.classList.add('is-interacting');
    selectNode(node);
  }

  function moveNodeDrag(e) {
    if (!drag || e.pointerId !== drag.pointerId) return;
    const p = boardPoint(e.clientX, e.clientY);
    drag.node.style.left = snap(p.x - drag.offsetX) + 'px';
    drag.node.style.top = snap(p.y - drag.offsetY) + 'px';
    updateWires();
  }

  function endNodeDrag(e) {
    if (!drag || e.pointerId !== drag.pointerId) return;
    drag.node.classList.remove('is-dragging');
    drag.node.releasePointerCapture(e.pointerId);
    drag = null;
    board.classList.remove('is-interacting');
    resizeBoard();
    updateWires();
  }

  function startPan(e) {
    if (tool !== 'pan' || e.button !== 0) return;
    if (e.target.closest('.media-node, .float-pill, .canvas-rail')) return;
    e.preventDefault();
    area.setPointerCapture(e.pointerId);
    pan = {
      pointerId: e.pointerId,
      x: e.clientX,
      y: e.clientY,
      scrollLeft: stage.scrollLeft,
      scrollTop: stage.scrollTop,
    };
    board.classList.add('is-panning');
  }

  function movePan(e) {
    if (!pan || e.pointerId !== pan.pointerId) return;
    stage.scrollLeft = pan.scrollLeft - (e.clientX - pan.x);
    stage.scrollTop = pan.scrollTop - (e.clientY - pan.y);
  }

  function endPan(e) {
    if (!pan || e.pointerId !== pan.pointerId) return;
    area.releasePointerCapture(e.pointerId);
    pan = null;
    board.classList.remove('is-panning');
  }

  function setTool(next) {
    tool = next;
    document.body.dataset.canvasTool = tool;
    railButtons.forEach((btn) => {
      const title = btn.getAttribute('title') || '';
      const isSelect = title === '选择' && tool === 'select';
      const isPan = title === '平移' && tool === 'pan';
      btn.classList.toggle('active', isSelect || isPan);
    });
  }

  nodes.forEach((node) => {
    node.addEventListener('pointerdown', (e) => startNodeDrag(node, e));
    node.addEventListener('click', (e) => {
      if (tool !== 'select' || drag) return;
      if (!isInteractive(e.target)) selectNode(node);
    });
  });

  document.addEventListener('pointermove', (e) => {
    moveNodeDrag(e);
    movePan(e);
  });

  document.addEventListener('pointerup', endNodeDrag);
  document.addEventListener('pointercancel', endNodeDrag);
  document.addEventListener('pointerup', endPan);
  document.addEventListener('pointercancel', endPan);

  area.addEventListener('pointerdown', startPan);

  railButtons.forEach((btn) => {
    const title = btn.getAttribute('title');
    if (title === '选择') btn.addEventListener('click', () => setTool('select'));
    if (title === '平移') btn.addEventListener('click', () => setTool('pan'));
  });

  window.addEventListener('resize', () => {
    if (board.classList.contains('layout-absolute')) updateWires();
  });

  setTool('select');
  applyInitialLayout();
})();
