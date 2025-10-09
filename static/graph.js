
    const canvas = document.getElementById('coordinatePlane');
    const ctx = canvas.getContext('2d');

    const scale = 40;
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;

    function getXserif(x,text) {
    ctx.beginPath();
    ctx.moveTo(x, centerY - 5);
    ctx.lineTo(x, centerY + 5);
    ctx.stroke()
    ctx.fillText(text,x-8,centerY-10)
}
    function getYserif(y,text) {
    ctx.beginPath();
    ctx.moveTo(centerX-5,y);
    ctx.lineTo(centerX+5,y);
    ctx.stroke();
    ctx.fillText(text,centerX+8,y);

}
    ctx.globalAlpha = 0.8;
    ctx.fillStyle="blue";
    ctx.fillRect(centerX, centerY, -scale, scale*2);
    const radius = 40;


    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, Math.PI, Math.PI/-2);
    ctx.lineTo(centerX, centerY)
    ctx.fillStyle = 'blue'
    ctx.lineWidth = 2;
    ctx.fill();


    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX+(scale), centerY);
    ctx.lineTo(centerX, centerY-scale*2);
    ctx.closePath();
    ctx.fillStyle = 'blue';
    ctx.fill();
    ctx.globalAlpha = 1;


    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(canvas.width, centerY);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX, canvas.height);
    ctx.stroke();
    ctx.lineWidth = 1;

    ctx.fillStyle='black';

    let x = centerX-40;
    getXserif(x,"R/2");
    x = centerX+40;
    getXserif(x,"R/2");
    x+=40;
    getXserif(x,"R");
    x-=160;
    getXserif(x,"R");
    let y = centerY+40;
    getYserif(y,"R/2");
    y+=40;
    getYserif(y,"R");
    y-=120;
    getYserif(y,"R/2");
    y-=40;
    getYserif(y,"R");




    ctx.font = '12px arial';
    ctx.fillStyle = '#000000';
    ctx.fillText('X', canvas.width - 10, centerY - 10);
    ctx.fillText('Y', centerX + 10, 10);