package demo.rx.gxj.com.myrx_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    TextView text, text2 ,text3;
    StringBuilder stringBuilder = new StringBuilder();
    OkHttpClient mOkHttpClient  ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(5000, TimeUnit.MILLISECONDS);

        text = (TextView) findViewById(R.id.text);
        text2 = (TextView) findViewById(R.id.text2);
        text3 = (TextView) findViewById(R.id.text3);

        Observable<String> myObservable = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> sub) {
                        sub.onNext("我是订阅者1");
                        sub.onCompleted();
                    }
                }
        );


        Subscriber<String> mySubscriber1 = new Subscriber<String>() {
            @Override
            public void onNext(String s) {
                stringBuilder.append("onNext " + s);
                text.setText(stringBuilder);
            }

            @Override
            public void onCompleted() {
                stringBuilder.append("onCompleted  ！" );
                text.setText(stringBuilder);
            }

            @Override
            public void onError(Throwable e) {
                text.setText("onError");
            }
        };

        myObservable.observeOn(AndroidSchedulers.mainThread())//在主线程中执行
                .subscribe(mySubscriber1);

        /**
         * 这里我们并不关心onError和onComplete，所以只需要第一个参数就可以
         * Observable.just就是用来创建只发出一个事件就结束的Observable对象，
         * 上面创建Observable对象的代码可以简化为一行
         */
        Observable.just("just 模式")//只有一个action，onNextAction
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        text2.setText(s);
                    }
                });


        Observable<String> myObservable2 = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> sub) {
                        sub.onNext("我是订阅者2");
                        sub.onCompleted();
                    }
                }
        );
        myObservable2.subscribe(new Action1<String>() {//onNextAction
            @Override
            public void call(String data) {
                text3.setText("Get Result:\r\n" + data);
            }
        }, new Action1<Throwable>() {//onErrorAction
            @Override
            public void call(Throwable throwable) {
                text3.setText("Get Error:\r\n" + throwable.getMessage());
            }
        });

    }

}
