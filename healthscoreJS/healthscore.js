var canvases = [];
var healths = [];
var dispHealths = [];
var showTexts = [];
var borderWidths = [];

var scores = 0;

var rate = .03;
var borderRadius = 8;
var fontName = 'helvetica';
var mercuryColor = '#C1272D';
var glassColor = '#E6E6E6';

function createHealthscore(healthScore, width, height, borderWidth, showText) {
  var idx = scores;
  scores++;

  var cvs = canvases[idx] = document.createElement('canvas');
  cvs.width = width;
  cvs.height = height;
  healths[idx] = healthScore;
  dispHealths[idx] = 0;
  borderWidths[idx] = borderWidth;
  showTexts[idx] = showText;
  window.requestAnimationFrame(function() {draw(idx);});
  return cvs;
}

function draw(idx) {
  var ctx = canvases[idx].getContext('2d');
  ctx.imageSmoothingEnabled = true;
  var w = canvases[idx].width;
  var h = canvases[idx].height;
  ctx.clearRect(0, 0, w, h);

  if(w > h)
    drawHoriziontal(ctx, w, h, dispHealths[idx], borderWidths[idx], showTexts[idx]);
  else
    drawVertical(ctx, w, h, dispHealths[idx], borderWidths[idx], showTexts[idx]);

  if(dispHealths[idx] != healths[idx]) {
    dispHealths[idx] += (healths[idx] - dispHealths[idx]) * rate;
    if(Math.abs(healths[idx] - dispHealths[idx]) < .005)
      dispHealths[idx] = healths[idx];
    window.requestAnimationFrame(function() {draw(idx);});
  }
}

function drawHoriziontal(ctx, w, h, health, borderWidth, showText) {
  ctx.fillStyle = glassColor;
  ctx.beginPath();
  ctx.arc(h/2, h/2, h/2, 0, 2*Math.PI);
  ctx.closePath();
  ctx.fill();

  ctx.roundRect(h/2, h/4-borderWidth, (w-(h/2)), h/2+2*borderWidth, borderRadius).fill();

  ctx.fillStyle = mercuryColor;
  ctx.beginPath();
  ctx.arc(h/2, h/2, h/2-borderWidth, 0, 2*Math.PI);
  ctx.closePath();
  ctx.fill();

  ctx.roundRect(h/2, h/4, (w-(h/2)-borderWidth)*health, h/2, borderRadius).fill();

  if(showText) {
    ctx.fillStyle = glassColor;
    ctx.font = Math.round(Math.min(w, h)/3)+'px '+fontName;
    var pct = Math.round(health * 100)+"%";
    //yes, I'm using the w of the letter 'M' to approximate the height of a string... it seems to work
    ctx.fillText(pct, (h/2) - (ctx.measureText(pct).width/2), h/2 + ctx.measureText('M').width/2);
  }
}

function drawVertical(ctx, w, h, health, borderWidth, showText) {
  ctx.fillStyle = glassColor;
  ctx.beginPath();
  ctx.arc(w/2, h-w/2, w/2, 0, 2*Math.PI);
  ctx.closePath();
  ctx.fill();
  ctx.roundRect(w/4-borderWidth, 0, w/2+2*borderWidth, h-w/2, borderRadius).fill();

  ctx.fillStyle = mercuryColor;
  ctx.beginPath();
  ctx.arc(w/2, h-w/2, w/2-borderWidth, 0, 2*Math.PI);
  ctx.closePath();
  ctx.fill();

  ctx.roundRect(w/4, borderWidth+(h-(w/2))*(1-health), w/2, h-w/2-(h-(w/2))*(1-health), borderRadius).fill();

  if(showText) {
    ctx.fillStyle = glassColor;
    ctx.font = Math.round(Math.min(w, h)/3)+'px '+fontName;
    var pct = Math.round(health * 100)+"%";
    ctx.fillText(pct, (w/2) - (ctx.measureText(pct).width/2), h - w/2 + ctx.measureText('M').width/2);
  }
}


CanvasRenderingContext2D.prototype.roundRect = function (x, y, w, h, r) {
  if (w < 2 * r) r = w / 2;
  if (h < 2 * r) r = h / 2;
  this.beginPath();
  this.moveTo(x+r, y);
  this.arcTo(x+w, y,   x+w, y+h, r);
  this.arcTo(x+w, y+h, x,   y+h, r);
  this.arcTo(x,   y+h, x,   y,   r);
  this.arcTo(x,   y,   x+w, y,   r);
  this.closePath();
  return this;
}