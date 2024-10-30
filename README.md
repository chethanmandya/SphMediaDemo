# SPHMedia - Demo App

**App Features**  
This app demonstrates how to implement a simple public information on breweries using the [Open Brewery DB API](https://www.openbrewerydb.org). Key features include:

- Pagination for efficient data loading
- Tab View & Horizontal paging (Similar to ViewPager and FragmentStateAdapter)
- LazyVerticalGrid (similar to RecyclerView in Jetpack Compose)
- Offline data supporting

### App Requirements

The following requirements are implemented:

1. **List Screen**: Displays a list of breweries using Jetpack Compose and loads data from the API, Implemented using compose pagination.
2. **Detail Screen**: On selecting an item, navigates to a detail screen showing more details.
3. **Responsive UI**: Supports for phones, tablets, and foldable devices.

### Good to Have Features :

- **Offline Support**: Implements offline caching to ensure data accessibility even without an internet connection. Cached data expires after 5 minutes and then fetches fresh data to update the content. This approach reduces redundant API calls to the server, enhancing both performance and resource efficiency.

### Technology Stack

The app leverages several modern Android components and libraries:

- **UI Components**:
- **paging-compose**
      `Paging-compose` efficiently loads large data sets in chunks, reducing memory usage and improving performance. It integrates with ViewModel to ensure data survives configuration changes and supports automatic updates as users scroll.

- **LazyVerticalGrid for Scrolling**
  `LazyVerticalGrid` creates a vertically scrolling grid layout, similar to RecyclerView, optimizing item composition for performance. It allows customizable item layouts and supports dynamic sizing for adaptable UI design.

- **HorizontalPaging**
  HorizontalPaging enables swiping through pages of content, providing a smooth navigation experience akin to ViewPager. It lazy loads content only when needed, enhancing performance and user experience.

- **ScrollableTabRow**
  `ScrollableTabRow` displays a horizontal row of tabs that can scroll if there are too many to fit on-screen. It facilitates quick navigation between different sections while providing visual feedback on the selected tab.


- **Architecture**:
    - **Clean Architecture (3-Layer Structure)**:
        - **App Layer** - Presentation layer: Contains the UI, ViewModel classes
        - **Domain Layer** - Business logic: Includes Repository classes, PagingSource
        - **Data Layer** - Network and database interactions: Room entities, DAOs, Retrofit interfaces
        - **Common Layer** -
          **Used MVVM** - Model view View Model segregation

- **Other Components**:
    - **Navigation Compose** for in-app navigation
    - **Room** for local database management
    - **Hilt** Hilt is Jetpack recommended dependency injection library, it builds on top of dagger. Dependency injection is technique widely used in programming and well suited for android developement where class dependencies are provided from outside of class instead inside of the class.
        - This reduce coupline in your code base
        - resuseability
        - Testing become easier

    - **Coroutines** for asynchronous operations

### Testing Strategy

- **Unit Tests**:
    -  Used Robolectric framework to run on the JVM for fast feedback
    -  Utilizes MockWebServer, Moshi, and JUnit4 for mocking.

- **UI Testing (Instrumentation)**:
    - **Android UI Testing APIs**: For testing Jetpack Compose UI interactions
Please NOTE : emulator must connect to Internet while running UI test case . 

### Compose State Management
This app is designed following best practices in state management and responsiveness, using **ViewModel** and **rememberSaveable** for state persistence. With its modularized architecture, it maintains separation of concerns and ease of testing.

Managing state in Android, especially with Jetpack Compose, can be challenging. While Compose offers powerful features, it has some limitations, including an issue with pagination not retaining scroll position when navigating to a detail screen and returning to the list. This is a known bug in the framework (see [Google issue tracker](https://issuetracker.google.com/issues/177245496)).

A common workaround involves using `rememberLazyGridState`, but even this fails to preserve scroll position, causing the list to reset to the top when revisiting. This behavior can disrupt user experience, as they lose their place within the list.

The currently recommended approach is to use `cacheIn(viewModel)` for the pagination flow. By caching data at the ViewModel level, the scroll position remains unaffected, as Compose reuses the cached data and restores the previous scroll state seamlessly. This approach ensures that even after navigating away, users return to the exact scroll position, regardless of list length. You can take look at BreweryListViewModel , the same solution adopted. 

### Screen shots / Video's

Unit Test and UI test Case Demo Video Link
https://drive.google.com/file/d/1vVeInlUThsebfyDtHnsJoOkzmS-Mpv2t/view?usp=sharing

Complete App Demo :
https://drive.google.com/file/d/1ZYwMsp_zw5uoduBnKKfJhnPWb0Kn_CxB/view?usp=sharing

Tablet UI :
https://drive.google.com/file/d/1aegsou9B813Qj-dRj69Df-gR_PAh2XjF/view?usp=sharing

Phone UI
https://drive.google.com/file/d/1266qghX3SUbyCUVk4bAE49y11F83GwQI/view?usp=sharing
