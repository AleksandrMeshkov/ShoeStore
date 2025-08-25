package com.example.shoe_store

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.shoe_store.models.BasketItem
import com.example.shoe_store.models.Product
import com.example.shoe_store.models.User
import com.example.shoe_store.ui.theme.Shoe_storeTheme
import com.example.shoe_store.utils.SessionManager

class Menu : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Shoe_storeTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object Favorites : Screen()
    object Cart : Screen()
    object Profile : Screen()
    data class ProductDetail(val productId: Int) : Screen()
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = {
                    currentScreen = Screen.Home
                    selectedProductId = null
                },
                onFavoritesClick = {
                    currentScreen = Screen.Favorites
                    selectedProductId = null
                },
                onCartClick = {
                    currentScreen = Screen.Cart
                    selectedProductId = null
                },
                onProfileClick = {
                    currentScreen = Screen.Profile
                    selectedProductId = null
                },
                currentScreen = currentScreen
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(
                    onProductClick = { productId ->
                        selectedProductId = productId
                        currentScreen = Screen.ProductDetail(productId)
                    }
                )

                is Screen.Favorites -> FavoritesScreen()
                is Screen.Cart -> CartScreen()
                is Screen.Profile -> ProfileScreen()
                is Screen.ProductDetail -> {
                    ProductDetailScreen(
                        productId = screen.productId,
                        onBack = {
                            currentScreen = Screen.Home
                            selectedProductId = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onProductClick: (Int) -> Unit) {
    val viewModel: HomeViewModel = viewModel()
    val products by viewModel.products.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)

    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = products.filter { product ->
        product.Name.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.menu),
            contentDescription = "Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { query -> searchQuery = query })
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Категории",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
            CategoriesSection()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Акции",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
            SalesSection()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Каталог",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (!error.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = error.orEmpty(), color = Color.Red)
                }
            } else {
                ProductList(products = filteredProducts, onProductClick = onProductClick)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProductList(products: List<Product>, onProductClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        products.chunked(2).forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowProducts.forEach { product ->
                    ProductItem(product = product, onProductClick = onProductClick)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProductItem(product: Product, onProductClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp)
            .clickable {
                onProductClick(product.ProductID)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.Photo,
                    contentDescription = product.Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "BEST SELLER",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = product.Name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${product.Price} руб",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}


@Composable
fun FavoritesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("Избранное", style = MaterialTheme.typography.headlineMedium)
    }
}


@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()
    var showEditScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedUser = SessionManager.getUser(context)
        savedUser?.UsersID?.let { userId ->
            viewModel.fetchUserById(context, userId)
        } ?: viewModel.loadUserFromSession(context)
    }

    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            showEditScreen = false
            viewModel.resetUpdateStatus()
            user?.UsersID?.let { userId ->
                viewModel.fetchUserById(context, userId)
            }
        }
    }

    if (showEditScreen && user != null) {
        EditProfileScreen(
            onBack = { showEditScreen = false },
            onSave = {  },
            viewModel = viewModel
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading || isUpdating -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    ErrorMessage(error = error!!, onRetry = {
                        viewModel.loadUserFromSession(context)
                    }, onLogin = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? Activity)?.finish()
                    })
                }

                user != null -> {
                    ProfileContent(
                        user = user!!,
                        context = context,
                        onEditClick = { showEditScreen = true }
                    )
                }

                else -> {
                    Text(
                        text = "Пользователь не авторизован",
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as? Activity)?.finish()
                        }
                    ) {
                        Text("Войти")
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String, onRetry: () -> Unit, onLogin: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Повторить попытку")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти")
        }
    }
}

@Composable
fun ProfileContent(user: User, context: Context, onEditClick: () -> Unit) {
    val safeName = user.Name ?: "Не указано"
    val safeSurname = user.Surname ?: "Не указано"
    val safePatronymic = user.Patronymic ?: ""
    val safeLogin = user.Login ?: "Не указано"
    val safePhoto = user.Photo

    val initials = remember(safeName, safeSurname) {
        buildString {
            safeName.firstOrNull()?.let { append(it) }
            safeSurname.firstOrNull()?.let { append(it) }
        }.takeIf { it.isNotEmpty() } ?: "?"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Профиль",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (!safePhoto.isNullOrEmpty()) {
                AsyncImage(
                    model = safePhoto,
                    contentDescription = "User Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }

        Text(
            text = "$safeName $safeSurname",
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            ProfileItem(label = "Имя", value = safeName)
            ProfileItem(label = "Фамилия", value = safeSurname)
            if (safePatronymic.isNotEmpty()) {
                ProfileItem(label = "Отчество", value = safePatronymic)
            }
            ProfileItem(label = "Логин", value = safeLogin)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Редактировать профиль")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                SessionManager.clearUser(context)
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? Activity)?.finish()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = "Выйти из аккаунта")
        }
    }
}


@Composable
fun ProfileItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value.ifEmpty { "Не указано" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf(user?.Name ?: "") }
    var surname by remember { mutableStateOf(user?.Surname ?: "") }
    var patronymic by remember { mutableStateOf(user?.Patronymic ?: "") }
    var login by remember { mutableStateOf(user?.Login ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isFormValid = name.isNotBlank() && surname.isNotBlank() && login.isNotBlank()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImageUri != null -> {
                    Image(
                        painter = rememberImagePainter(selectedImageUri),
                        contentDescription = "Новое фото",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                !user?.Photo.isNullOrEmpty() -> {
                    AsyncImage(
                        model = user?.Photo,
                        contentDescription = "Текущее фото",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    val initials = remember(name, surname) {
                        buildString {
                            name.firstOrNull()?.let { append(it) }
                            surname.firstOrNull()?.let { append(it) }
                        }.takeIf { it.isNotEmpty() } ?: "?"
                    }
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Изменить фото",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
                    .padding(4.dp),
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя*") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = name.isBlank(),
            supportingText = {
                if (name.isBlank()) {
                    Text("Обязательное поле")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Фамилия*") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = surname.isBlank(),
            supportingText = {
                if (surname.isBlank()) {
                    Text("Обязательное поле")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = patronymic,
            onValueChange = { patronymic = it },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин*") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = login.isBlank(),
            supportingText = {
                if (login.isBlank()) {
                    Text("Обязательное поле")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Новый пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("Оставьте пустым, если не хотите менять") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    user?.UsersID?.let { userId ->
                        viewModel.updateUserProfile(
                            context = context,
                            userId = userId,
                            login = login,
                            password = password,
                            name = name,
                            surname = surname,
                            patronymic = patronymic,
                            photoUri = selectedImageUri
                        )
                    }
                } else {
                    viewModel.setError("Заполните обязательные поля")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isUpdating && isFormValid
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Сохранить изменения")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("Отмена")
        }
    }
}

private fun ProfileViewModel.setError(string: String) {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .background(Color.White, shape = RoundedCornerShape(8.dp)),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            placeholder = { Text("Поиск") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.LightGray, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.filtr),
                contentDescription = "main",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun CategoriesSection() {
    val categories = listOf("Все", "Outdoor", "Tennis", "Running")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = category,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SalesSection() {
    val salesImages = listOf(
        R.drawable.sale_icn,
        R.drawable.sale_icn,
        R.drawable.sale_icn
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        salesImages.forEach { imageRes ->
            Box(
                modifier = Modifier
                    .size(width = 335.dp, height = 95.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Sale",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ProductDetailScreen(productId: Int, onBack: () -> Unit) {
    val viewModel: ProductDetailViewModel = viewModel()
    val product by viewModel.product.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)

    LaunchedEffect(productId) {
        viewModel.fetchProductById(productId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error.orEmpty(), color = Color.Red)
            }
        } else if (product != null) {
            ProductDetailContent(product = product!!, onBack = onBack)
        }
    }
}

@Composable
fun ProductDetailContent(product: Product, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: BasketViewModel = viewModel()
    val userId = SessionManager.getUser(context)?.UsersID ?: 0
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
                .offset(x = (-16).dp, y = (-16).dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = product.Photo,
                contentDescription = product.Name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.Name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${product.Price} руб.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.Description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Подробнее",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (userId != 0) {
                        viewModel.addToBasket(userId, product.ProductID)
                    } else {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? Activity)?.finish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("В корзину", fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    currentScreen: Screen
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.home_icon),
                    contentDescription = "Главная"
                )
            },
            label = { Text("Главная") },
            selected = currentScreen is Screen.Home,
            onClick = onHomeClick
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.like_icn),
                    contentDescription = "Избранное"
                )
            },
            label = { Text("Избранное") },
            selected = currentScreen is Screen.Favorites,
            onClick = onFavoritesClick
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.basket),
                    contentDescription = "Корзина"
                )
            },
            label = { Text("Корзина") },
            selected = currentScreen is Screen.Cart,
            onClick = onCartClick
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.profile_icon),
                    contentDescription = "Профиль"
                )
            },
            label = { Text("Профиль") },
            selected = currentScreen is Screen.Profile,
            onClick = onProfileClick
        )
    }
}

@Composable
fun CartScreen() {
    val context = LocalContext.current
    val viewModel: BasketViewModel = viewModel()
    val basketItems by viewModel.basketItems.observeAsState(emptyList())
    val total by viewModel.total.observeAsState(0.0)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)

    val userId = SessionManager.getUser(context)?.UsersID ?: 0

    LaunchedEffect(userId) {
        if (userId != 0) {
            viewModel.fetchBasket(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Корзина",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (userId == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Для просмотра корзины необходимо авторизоваться")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as? Activity)?.finish()
                        }
                    ) {
                        Text("Войти")
                    }
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error.orEmpty(), color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchBasket(userId) }) {
                        Text("Повторить попытку")
                    }
                }
            }
        } else if (basketItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Корзина пуста")
            }
        } else {
            Text(
                text = "${basketItems.size} товара",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                basketItems.forEach { item ->
                    BasketItemCard(
                        item = item,
                        onIncrease = { viewModel.increaseQuantity(userId, item) },
                        onDecrease = { viewModel.decreaseQuantity(userId, item) },
                        onRemove = { viewModel.removeFromBasket(userId, item.ProductID) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Сумма")
                    Text("%.2f руб.".format(total))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Доставка")
                    Text("500 руб.")
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Итого",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "%.2f руб.".format(total + 60.20),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Оформить заказ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BasketItemCard(
    item: BasketItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.product.Photo,
                    contentDescription = item.product.Name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.product.Name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.product.Price} руб.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.product.Description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Уменьшить количество"
                    )
                }

                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodyLarge
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Увеличить количество"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Удалить",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    Shoe_storeTheme {
        MainScreen()
    }
}