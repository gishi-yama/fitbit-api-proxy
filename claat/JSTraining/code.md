code01::
```html
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
</head>
<body>
<h1>JavaScriptのトレーニング</h1>

<p>あなたの学籍番号：<span id="gakuseki">?</span></p>
<p>計算結果（加算）：<span id="add">?</span></p>
<p>計算結果（乗算）：<span id="multiply">?</span></p>
<button id="btn" onclick="push()">ボタンを押してください</button>
<p>ボタン：<span id="atPushed">押してください</span></p>

<script type="text/javascript">
  'use strict';

  // ここからJSのスクリプトを書く

</script>
</body>
</html>
```

code02::
```html
<script type="text/javascript">
  'use strict';

  // ここからJSのスクリプトを書く

</script>
```

code03::
```html
<script type="text/javascript">
  'use strict';

  // ここからJSのスクリプトを書く
  alert("1番目の処理");

</script>
```

code04::
```js
// ここからJSのスクリプトを書く
alert("1番目の処理");
alert("2番目の処理");
alert("3番目の処理");
```


code05::
```
// ここからJSのスクリプトを書く
// alert("1番目の処理");
// alert("2番目の処理");
// alert("3番目の処理");
```

code06::
```js
// ここからJSのスクリプトを書く
let quiz = "16+2は";
alert(quiz);
```

code07::
```js
// ここからJSのスクリプトを書く
let quiz = "16+2は";
alert(quiz);
let ans = 16 + 2;
alert(ans);
```


code08::
```js
// ここからJSのスクリプトを書く
// let quiz = "16+2は";
// alert(quiz);
// let ans = 16 + 2; 
// alert(ans);

let myNumber = "b1992490";
document.getElementById("gakuseki").innerHTML = myNumber;
```

code09::
```js
let added = 5 + 5;
document.getElementById("add").innerHTML = added;
let multiplied = 5 * 5;
document.getElementById("multiply").innerHTML = multiplied;
```

code0a::
```js
// let myNumber = "b1992490";
// document.getElementById("gakuseki").innerHTML = myNumber;
// let added = 5 + 5;
// document.getElementById("add").innerHTML = added;
// let multiplied = 5 * 5;
// document.getElementById("multiply").innerHTML = multiplied;

let myNumber = "b1992490";
rewrite("gakuseki", myNumber);
let added = 6 + 3;
rewrite("add", added);
let multiplied = 6 * 3;
rewrite("multiply", multiplied);

function rewrite(id, arg) {
  document.getElementById(id).innerHTML = arg;
}
```

code0b::
```js
function push() {
  let message = "押されました！";
  rewrite("atPushed", message);
}
```