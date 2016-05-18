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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    TextView text, text2 ,text3, text4;
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
        text4 = (TextView) findViewById(R.id.text4);

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


        /**
         * subscriber并不关心返回的字符串，而是想要字符串的hash值
         * 它不必返回Observable对象返回的类型，你可以使用map操作符返回一个发出新的数据类型的observable对象
         *
         * 在一个Observable对象上多次使用map操作符，最终将最简洁的数据传递给Subscriber对象。
         * Observable和Subscriber中间可以增减任何数量的map。
         */
        Observable.just("Map : " )
                .map(new Func1<String, Integer>() {

                    @Override
                    public Integer call(String s) {
                        return s.hashCode();//通过第一个map转成Integer
                    }
                })
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer s) {
                        return Integer.toString(s);//再通过第二个map转成String
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        text4.setText(s);
                    }
                });

         //看个例子,将集合的数据都输出:
        List<String> s = Arrays.asList("Java", "Android", "Ruby", "Ios", "Swift");
        Observable.from(s).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });


        /**
         * map 是在一个 item 被发射之后，到达 map 处经过转换变成另一个 item ，然后继续往下走；
         * flapMap 是 item 被发射之后，到达 flatMap 处经过转换变成一个 Observable ，
         * 而这个 Observable 并不会直接被发射出去，而是会立即被激活，
         * 然后把它发射出的每个 item 都传入流中，再继续走下去。
         *
         所以 flatMap 和 map 有两个区别：
         1.经过 Observable 的转换，相当于重新开了一个异步的流；
         2.item 被分散了，个数发生了变化。
         原文链接：http://www.jianshu.com/p/88779bda6691
         */
        // 注意这里的参数是 query所返回的Observable的输出,并且返会一个Observable<String>
        query().flatMap(new Func1<List<String>, Observable<String>>() {
            @Override
            public Observable<String> call(List<String> strings) {
                //结合from处理
                return Observable.from(strings);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println("_flatMap:"+s);
            }
        });

        query().flatMap(new Func1<List<String>, Observable<String>>() {
            @Override
            public Observable<String> call(List<String> strings) {
                return Observable.from(strings);
            }
        }).flatMap(new Func1<String, Observable<String>>() {
            @Override
            public Observable<String> call(String s) {
                //我们在这里调用`addPre`方法,就行处理
                return addPre(s);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });


    }

    static Observable<List<String>>query(){
        List<String> s = Arrays.asList("Java", "Android", "Ruby", "Ios", "Swift");
        return Observable.just(s);
    }

    static Observable<String>addPre(String lan){
        return Observable.just("addPre_"+lan);
    }

}
