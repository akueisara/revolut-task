# Simple Currency Converter


This app fetches and displays up-to-date conversion rates for a list of several world currencies from Revolut’s API and uses MVVM architecture

I decided to use an asynchronous method to retrieve the rates at 1-second intervals. When the user taps on a currency; it is shifted to the top of the list, user input is enabled, and then all the other currency rows will update with the most recent rates.

When there is no internet connectivity, the app will provide offline conversions based on the most recent base-rate data from the API. These rates are saved in a local database and updated whenever internet connectivity is restored.

I added a notification panel at the bottom of screen to remind the user to refresh if there is no internet.

## Screenshots

![Left: Online, Right: Offline](https://github.com/akueisara/revolut-task/blob/master/screens.png?raw=true)


## Libraries used:
* **Glide** for image loading and caching of the currency icons
* **Timber** for logging
* **Retrofit** & **OKHttp** & **Moshi** for networking
* **Couroutines** for asynchronous io (api & database)


## Still to-do / Ideas for improvement:

* **Fix Keyboard hiding in landscape mode.**

  * When the phone is in landscape orientation, the keyboard automatically hides whenever the data in RecyclerView is refreshed. I think this can be solved by adding keyboard control programmatically based on some simple heuristic. 

* **Add unit tests** mainly using *Expresso* (instrumented unit tests) & the *JUnit4* framework(local unit tests)

* I also think it would be useful to **add a horizontal A-to-Z scroll bar** under the title, so the user can quickly search for a particular currency.

## Credits:

* <div>Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/"     title="Flaticon">www.flaticon.com</a></div>
