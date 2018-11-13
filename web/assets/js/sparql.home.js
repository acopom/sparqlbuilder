$(function($) {
    // Init
    var query = getCookie('sparql-query');
    var ep = getCookie('sparql-ep');
    var is_query = query && ep ? true : false;

    // SPARQLが取得済みか
    if(is_query){
        $('.js-sparql-query').val(query);
        $('.btn-sparql-send, .btn-sparql-download').addClass('blue').removeClass('default btn-outline');
        $('.js-sparql-help').show();
    }else{
        $('.btn-sparql-send, .btn-sparql-download').removeClass('blue').addClass('default btn-outline');
    }


    /**
     * Send ボタン処理
     */
    $('.js-sendSPARQL').on('click', function(){
        if (!is_query) {
            return;
        }
        query = encodeURIComponent(query);
        openpage = ep + "?format=text%2Fhtml&query=" + query;
        window.open(openpage);
    });


    /**
     * Download ボタン処理
     */
    $('.js-downloadResult').on('click', function(){
        if (!is_query) {
            return;
        }

        qr = sendQuery(ep, query);
        qr.fail(
            function(xhr, textStatus, thrownError) {
                alert("Error: A '" + textStatus + "' occurred.");
            }
        );
        qr.done(
            function(d) {
                downloadCSV(d.results.bindings);
            }
        );
    });


    function downloadCSV(data){

    	if (data instanceof Array) {
    		var result_txt ="";

    		var i=0;
    		for ( var key in data[0]) {
    			if(i>0){result_txt +=",";}
    			result_txt += key;
    			i++;
    		}

    		result_txt += "\n";

    		for (var d = 0; d < data.length; d++) {
    			var i = 0;
    			for ( var key in data[d]) {
    				if(i>0){result_txt +=",";}
    				result_txt += data[d][key].value;
    				i++;
    			}
    			result_txt += '\n';
    		}

    		var blob = new Blob( [result_txt], {type: 'text/plain'} )

    		var link = document.createElement('a')
    		link.href = URL.createObjectURL(blob)
    		link.download = 'result' + '.csv'

    		document.body.appendChild(link) // for Firefox
    		link.click()
    		document.body.removeChild(link) // for Firefox
    	}
    };


    /**
     * Sample ボタン処理
     * @return {[type]} [description]
     */
    $('.js-openSample').on('click', function(){
        var ep = $(this).attr('data-ep');
        var st = $(this).attr('data-st');
        var en = $(this).attr('data-en');
        window.open('/sparql/?ep='+encodeURIComponent(ep)+'&st='+encodeURIComponent(st)+'&en='+encodeURIComponent(en), "_self");
    });


    /**
     * クッキーの読み込み
     */
    function getCookie(value) {
        var c_data = document.cookie + ";";
        c_data = unescape(c_data);
        var n_point = c_data.indexOf(value);
        var v_point = c_data.indexOf("=", n_point) + 1;
        var end_point = c_data.indexOf(";", n_point);
        if (n_point > -1) {
            c_data = c_data.substring(v_point, end_point);
            return c_data;
        }
        return false;
    }

    /**
     * クッキーの書き込み
     */
    function setCookie(cookie_name, value) {
        var _deadtime = 240;
        var ex = new Date();
        ex.setHours(ex.getHours() + _deadtime);
        ex = ex.toGMTString();
        var c = escape(cookie_name) + "=" + escape(value) + ";expires=" + ex;
        document.cookie = c;
    }


    /**
     JavascriptのみでSPARQLクエリを検索可能なライブラリ
    */

    // from sgvizler.js version 0.6
    var sendQuery = function (e,q,f,t) {
    	if (typeof f==="undefined") f="json";
    	if (typeof t==="undefined") t=f;
        var promise;

        if (window.XDomainRequest) {
            // special query function for IE. Hiding variables in inner function.
            // TODO See: https://gist.github.com/1114981 for inspiration.
            promise = (
    			function () {
        			/*global XDomainRequest */
        			var qx = $.Deferred(),
            		xdr = new XDomainRequest(),
            		url = e +
    				"?query=" + q +
    				"&output=" + t;
        			xdr.open("GET", url);
    				xdr.onload = function () {
    					var data;
            			if (myEndpointOutput === qfXML) {
    						data = $.parseXML(xdr.responseText);
            			} else {
    						data = $.parseJSON(xdr.responseText);
            			}
            			qx.resolve(data);
    				};
        			xdr.send();
        			return qx.promise();
    			}()
            );
        } else {
            promise = $.ajax({
    			url: e,
    			headers: {
    				"Accept": "application/sparql-results+json"
    			},
    			data: {
    				query: q,
    				output: f
    			},
    			dataType: t
            });
        }
        return promise;
    }
});
