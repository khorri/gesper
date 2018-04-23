/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

  window.onload = function() {

    var mapElement = document.getElementById('map');
    var loadingImage = document.getElementById('loadingImage');
    mapElement.style.visibility="hidden";
    loadingImage.style.display="inline";
	    
    var options = {
      zoom: 3,
      center: new google.maps.LatLng(48.8357120, 2.5856770),
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    
    var map = new google.maps.Map(document.getElementById('map'), options);
    var query = window.location.search.substring(1);
    query =  decodeURIComponent(query);
    var appHome = location.protocol + '//' + location.host + '/' + location.pathname.split('/')[1];
    
  	function getQueryVariable(variable) {		
		var vars = query.split("&");
		for ( var i = 0; i < vars.length; i++) {
			var pair = vars[i].split("=");
			if (pair[0] == variable) {
				return pair[1];
			}
		}
		return (false);
	}
    
    google.maps.event.addListenerOnce(map, 'bounds_changed', function() {
      
      // Creating an array that will store the markers
      var markers = [];
      // To show Object details on click of Pinpoint
      var infowindow = new google.maps.InfoWindow();
      
		var requestP = $.ajax({
			  type: "GET",
			  contentType: 'application/json',
			  url: appHome + "/ws/map/" + getQueryVariable("object")
			});
			
		requestP.done(function(result) {
		
		  if (result.status == 0) {		   		  	   		  		
							
		    for (var i = 0; i < result.data.length; i++) {
		    
		        var d = result.data[i];
		    	var icon = 'http://thydzik.com/thydzikGoogleMap/markerlink.php?text='+d['pinChar'];
		    	var latlng = new google.maps.LatLng(d['latit'], d['longit']);		    
				var title = d['fullName'].trim();	
				var content = "<b>" + title + "</b></br>" + d['address'] + "</br><i class='fa fa-phone'></i>&nbsp;" + d['fixedPhone'];
				content = content + "</br><i class='fa fa-envelope'></i>&nbsp;" + d['emailAddress'];
				var iconcolor;
				
				switch (d['pinColor']) {
				case 'blue':
					iconcolor = '5680FC';				
					break;
				case 'gray' :
					iconcolor = 'A8A8A8 ';
					break;			
				case 'orange':
					iconcolor = 'EF9D3F';
					break;
				case 'yellow':
					iconcolor = 'FCF356';
					break;
				case 'purple':
					iconcolor = '7D54FC';
					break;
				case 'pink':
					iconcolor = 'E14E9D';
					break;
				case 'brown':
					iconcolor = '9E7151';
					break;				
				default:
					iconcolor = 'FC6355';				
					break;
				}
				
				icon = icon + '&color=' + iconcolor;
				
				var marker = new google.maps.Marker({
				  position: latlng,
				  map: map,
				  title: title.split('</br>')[0],
				  icon: icon
				});

				google.maps.event.addListener(marker, 'click', (function(content) {	
				  return function(){
				   infowindow.setContent(content);
				   infowindow.open(map,this);
				  };      
				})(content));        	
				
				markers.push(marker);
		   
		    } //end loop	
		  } // end if
		});
			
		requestP.fail(function(jqXHR, textStatus) {
			  alert( "Request failed: " + textStatus );
		});
		
		requestP.complete(function() {		
			mapElement.style.visibility="visible";
			loadingImage.style.display="none";
		});
      
      var markerclusterer = new MarkerClusterer(map, markers);
          
    });    
  };       	
})();