code01::
```html
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.7.20/c3.min.css" rel="stylesheet">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/5.16.0/d3.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.7.20/c3.min.js"></script>
</head>
<body>
<p>当日0時から24時の山川の心拍数を取得し、グラフ化します。</p>
<p>新しいデータは約15分ごとに自動的に配信されます。</p>
<p>FitBitを外している時、通信状態が悪い時には、データが更新されなかったり、記録されているデータが飛び飛びになることもあります（できるだけ発生しないようには心がけます）。
<p>最後にデータが配信されたのは <span id="refreshAt">?</span> です。</p>

<div id="chart"></div>
<button id="btn" onclick="connect()">接続</button>

<script type="text/javascript">

  // xxxxxxxx の部分は、あなたの学籍番号に書き換える
  let ep = "wss://mcom.jp.ngrok.io/edge?gakuseki=xxxxxxxx";

  // ボタンを押したときに動く関数
  function connect() {

    // WebSocket通信を準備する
    

    // WebSocket でAPIにアクセスしたときに動く関数
   

    // WebSocket から情報が流れてきたときに動く関数
    
  };
</script>
</body>
</html>
```

code02::
```js
<script type="text/javascript">

  // xxxxxxxx の部分は、あなたの学籍番号に書き換える
  let ep = "wss://mcom.jp.ngrok.io/edge?gakuseki=xxxxxxxx";

  // ボタンを押したときに動く関数
  function connect() {

    // WebSocket通信を準備する
    

    // WebSocket でAPIにアクセスしたときに動く関数
   

    // WebSocket から情報が流れてきたときに動く関数
    
  };
</script>
```

code03::
```js
  // xxxxxxxx の部分は、あなたの学籍番号に書き換える
  let ep = 'wss://mcom.jp.ngrok.io/edge?gakuseki=xxxxxxxx';
```

code04::
```js
    // WebSocket通信を準備する
    let ws = new WebSocket(ep);
```

code05::
```js
    ws.onopen = function () {
alert("WebSocketで接続しました");
// ボタン用のHTMLを消す
let btnTag = document.getElementById('btn');
btnTag.style.display = 'none';
    };
```

code06::
```js
let btnTag = document.getElementById('btn');
btnTag.style.display = 'none';
```


code07::
```js
    // WebSocket から情報が流れてきたときに動く関数
    ws.onmessage = function (evt) {

// WebSocket から取得した情報を JSON 形式にする
let jsonData = JSON.parse(evt.data);
alert("データ件数:" + jsonData.length);

// この先は、次の段階で記述する

    };
```

code08::
```js
// alert("WebSocketで接続しました");
console.log("WebSocketで接続しました");
```

code09::
```js
// alert("データ件数:" + jsonData.length)
console.log("データ件数:" + jsonData.length);
```

code0a::
```js
<script type="text/javascript">

  // xxxxxxxx の部分は、あなたの学籍番号に書き換える
  let ep = "wss://mcom.jp.ngrok.io/edge?gakuseki=xxxxxxxx";

  // ボタンを押したときに動く関数
  function connect() {

    // WebSocket通信用を準備する
    let ws = new WebSocket(ep);

    // WebSocket でAPIにアクセスしたときに動く関数
    ws.onopen = function () {
console.log("WebSocketで接続しました");
// ボタン用のHTMLを消す
let btnTag = document.getElementById("btn");
btnTag.style.display = "none";
    };

    // WebSocket から情報が流れてきたときに動く関数
    ws.onmessage = function (evt) {

// WebSocket から取得した情報を JSON 形式にする
let jsonData = JSON.parse(evt.data);
console.log("データ件数:" + jsonData.length)

// この先は、次の段階で記述する

    };
  };
</script>
```

code0b::
```js
// この先は、次の段階で記述する
// JSonデータの一番最後の時間を取得して、HTMLの refreshAt に表示する
let lastOne = jsonData.slice(-1)[0];
let refreshTag = document.getElementById("refreshAt");
refreshTag.innerHTML = lastOne.time;

// この先は、グラフを表示する
```

code0c::
```js
let lastOne = jsonData.slice(-1)[0];
```

code0d::
```js
let refreshTag = document.getElementById("refreshAt");
refreshTag.innerHTML = lastOne.time;
```

code0f::
```js
// この先は、グラフを表示する
// グラフを表示する
c3.generate({
  bindto: "#chart",
  data: {
    json: jsonData,
    keys: {
      x: "time", value: ["value"]
    },
    names: {
      value: "心拍数"
    }
  },
  axis: {
    x: {
      label: {text: "時刻"},
      type: "category",
      tick: {culling: true, fit: false, width: 200}
    },
    y: {
      label: {text: "心拍数"},
      max: 160,
      min: 40
    }
  },
  grid: {
    y: {
      lines: [
        {value: 90, text: "↑カロリー燃焼", position: "start"},
        {value: 56, text: "↓安静時", position: "end"}
      ]
    }
  }
});
```

code0f
```js
<script type="text/javascript">

  // xxxxxxxx の部分は、あなたの学籍番号に書き換える
  let ep = "wss://mcom.jp.ngrok.io/edge?gakuseki=xxxxxxxx";

  // ボタンを押したときに動く関数
  function connect() {
    // WebSocket通信用を準備する
    let ws = new WebSocket(ep);

    // WebSocket でAPIにアクセスしたときに動く関数
    ws.onopen = function () {
      console.log("WebSocketで接続しました");
      // ボタン用のHTMLを消す
      let btnTag = document.getElementById("btn");
      btnTag.style.display = "none";
    };

    // WebSocket から情報が流れてきたときに動く関数
    ws.onmessage = function (evt) {

      // WebSocket から取得した情報を JSON 形式にする
      let jsonData = JSON.parse(evt.data);
      console.log("データ件数:" + jsonData.length)

      // JSonデータの一番最後の時間を取得して、HTMLの refreshAt に表示する
      let lastOne = jsonData.slice(-1)[0];
      let refreshTag = document.getElementById("refreshAt");
      refreshTag.innerHTML = lastOne.time;

      // グラフを表示する
      c3.generate({
        bindto: "#chart",
        data: {
          json: jsonData,
          keys: {
            x: "time", value: ["value"]
          },
          names: {
            value: "心拍数"
          }
        },
        axis: {
          x: {
            label: {text: "時刻"},
            type: "category",
            tick: {culling: true, fit: false, width: 200}
          },
          y: {
            label: {text: "心拍数"},
            max: 160,
            min: 40
          }
        },
        grid: {
          y: {
            lines: [
              {value: 90, text: "↑カロリー燃焼", position: "start"},
              {value: 56, text: "↓安静時", position: "end"}
            ]
          }
        }
      });
      
    };
  };
</script>
```