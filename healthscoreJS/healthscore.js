var canvases = [];
var healths = [];
var dispHealths = [];

var scores = 0;

var rate = .03;
var borderWidth = 3;
var borderRadius = 8;
var fontName = 'helvetica';
var mercuryColor = '#C1272D';
var glassColor = '#E6E6E6';

function createHealthscore(healthScore, width, height) {
  var score = scores;
  scores++;


  var cvs = canvases[score] = document.createElement('canvas');
  cvs.width = width;
  cvs.height = height;
  healths[score] = healthScore;
  dispHealths[score] = 0;
  window.requestAnimationFrame(function() {draw(score);});
  return cvs;
}

function draw(score) {
  var ctx = canvases[score].getContext('2d');
  ctx.imageSmoothingEnabled = true;
  var w = canvases[score].width;
  var h = canvases[score].height;
  ctx.clearRect(0, 0, w, h);

  if(w > h)
    drawHoriziontal(ctx, w, h, dispHealths[score]);
  else
    drawVertical(ctx, w, h, dispHealths[score]);

  if(dispHealths[score] != healths[score]) {
    dispHealths[score] += (healths[score] - dispHealths[score]) * rate;
    if(Math.abs(healths[score] - dispHealths[score]) < .005)
      dispHealths[score] = healths[score];
    window.requestAnimationFrame(function() {draw(score);});
  }
}

function drawHoriziontal(ctx, w, h, health) {
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

  ctx.fillStyle = glassColor;
  ctx.font = Math.round(Math.min(w, h)/3)+'px '+fontName;
  var pct = Math.round(health * 100)+"%";
  //yes, I'm using the w of the letter 'M' to approximate the height of a string... it seems to work
  ctx.fillText(pct, (h/2) - (ctx.measureText(pct).width/2), h/2 + ctx.measureText('M').width/2);
}

function drawVertical(ctx, w, h, health) {
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

  ctx.fillStyle = glassColor;
  ctx.font = Math.round(Math.min(w, h)/3)+'px '+fontName;
  var pct = Math.round(health * 100)+"%";
  ctx.fillText(pct, (w/2) - (ctx.measureText(pct).width/2), h - w/2 + ctx.measureText('M').width/2);
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