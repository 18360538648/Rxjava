package com.lsw.rxdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useFlowable();
    }

    /**
     * 简单的rxJava2使用
     */
    private void simpleRxJava2() {
        // 创建被观察者
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("1111");
                e.onNext("2222");
                e.onNext("3333");
                // next完成之后一定要complete
                e.onComplete();
            }
        });
        // 创建观察者
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i("lsw", "onSubscribe---");
            }

            @Override
            public void onNext(String s) {
                Log.i("lsw", "onNext---" + s);
            }


            @Override
            public void onError(Throwable e) {
                Log.i("lsw", "onError---");
            }

            @Override
            public void onComplete() {
                Log.i("lsw", "onComplete--");
            }
        };
        // 被观察者订阅观察者
        observable.subscribe(observer);
    }

    /**
     * 简单的rxJava2链式写法
     */
    private void simpleRxJava2Link() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                // ObservableEmitter发射器
                e.onNext("1111");
                Log.i("lsw", "Emitter111");
                e.onNext("2222");
                Log.i("lsw", "Emitter222");
                e.onNext("3333");
                Log.i("lsw", "Emitter3333");
                e.onNext("44444");
                Log.i("lsw", "Emitter44444");
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i("lsw", "onSubscribe---");
            }

            @Override
            public void onNext(String s) {
                Log.i("lsw", "onNext---" + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.i("lsw", "onError---");
            }

            @Override
            public void onComplete() {
                Log.i("lsw", "onComplete--");
            }
        });
    }

    /**
     * 简单的rxJava2使用--改变线程
     */
    private void simpleRxJava2ChangeThread() {
        // 创建被观察者
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.i("lsw", "observable***" + Thread.currentThread().getName());
                e.onNext("1111");
                e.onNext("2222");
                e.onNext("3333");
                // next完成之后一定要complete
                e.onComplete();
            }
        });
        // 创建观察者
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i("lsw", "onSubscribe---");
            }

            @Override
            public void onNext(String s) {
                Log.i("lsw", "observer" + Thread.currentThread().getName());
                Log.i("lsw", "onNext---" + s);
            }


            @Override
            public void onError(Throwable e) {
                Log.i("lsw", "onError---");
            }

            @Override
            public void onComplete() {
                Log.i("lsw", "onComplete--");
            }
        };
        // 被观察者订阅观察者
        observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * Map函数使用
     */
    public void mapUseDemo() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(11);
                e.onNext(22);
                e.onComplete();
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(@NonNull Integer integer) throws Exception {
                return "this result is " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.i("lsw", "s----" + s);
            }
        });
    }

    /**
     * FlatMap使用
     */
    public void flatMapDemo() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(11);
                e.onNext(22);
                e.onNext(33);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < 3; i++) {
                    list.add("i am value" + integer);
                }
                return Observable.fromIterable(list).delay(100, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.i("lsw", "s:" + s);
            }
        });
    }

    /**
     * zip函数
     */
    public void zipDemo() {
        Observable<Integer> observalbe1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                Log.i("lsw", "1");
                e.onNext(2);
                Log.i("lsw", "2");
                e.onNext(3);
                Log.i("lsw", "3");
                e.onNext(4);
                Log.i("lsw", "4");
                e.onComplete();
                Log.i("lsw", "onComplete A");
            }
        }).subscribeOn(Schedulers.io());
        Observable<String> observalbe2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("A");
                Log.i("lsw", "A");
                e.onNext("B");
                Log.i("lsw", "B");
                e.onNext("C");
                Log.i("lsw", "C");
                e.onComplete();
                Log.i("lsw", "onComplete B");
            }
        }).subscribeOn(Schedulers.io());
        Observable.zip(observalbe1, observalbe2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.i("lsw", "s:" + s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.i("lsw", "onComplete");
            }
        });

    }

    /**
     * Flowable使用
     */
    public void useFlowable() {
        // 与observable创建方式不同的是后面加了一个BackpressureStrategy.ERROR
        // BackpressureStrategy.ERROR会在出现上下游流速不均衡的时候直接抛出一个异常
        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
//                e.onNext("11");
//                Log.i("lsw", "onComplete-----");
//                e.onNext("22");
//                Log.i("lsw", "22-----");
//                e.onNext("33");
//                Log.i("lsw", "33-----");
//                e.onComplete();
//                Log.i("lsw", "onComplete flowable-----");
                for (int i = 0; i <= 228; i++) {
                    Log.i("lsw", "i" + i);
                    e.onNext("" + i);
                }
            }
        }, BackpressureStrategy.ERROR).
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        // 响应式拉取，观察者需要多少个
//                s.request(1);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.i("lsw", "s" + s);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        Log.i("lsw", "onComplete-----");
                    }
                });

    }
}
