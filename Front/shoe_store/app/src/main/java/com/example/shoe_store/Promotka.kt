package com.example.shoe_store

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoe_store.ui.theme.Shoe_storeTheme

class Promotka : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Shoe_storeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Welcome()
                }
            }
        }
    }
}

@Composable
fun Welcome() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ScreenOne()
                    1 -> ScreenTwo()
                    2 -> ScreenThree()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                CustomPagerIndicator(
                    totalPages = 3,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomButton(
                    page = pagerState.currentPage,
                    context = context,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CustomPagerIndicator(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            val color = if (index == currentPage) Color(0xFFFFA500) else Color.LightGray
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 6.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun CustomButton(
    page: Int,
    context: Context,
    modifier: Modifier = Modifier
) {
    val buttonText = when (page) {
        0 -> "Начать"
        1, 2 -> "Далее"
        else -> ""
    }

    Box(
        modifier = modifier
            .clickable {
                if (page == 2) {
                    context.startActivity(Intent(context, Menu::class.java))
                }
            }
            .background(Color.Transparent)
    ) {
        Image(
            painter = painterResource(id = R.drawable.button),
            contentDescription = buttonText,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = buttonText,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScreenOne() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.listone),
            contentDescription = "Welcome screen",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ScreenTwo() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.listtwo),
            contentDescription = "Second screen",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ScreenThree() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.listthree),
            contentDescription = "Last screen",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PromotkaPreview() {
    Shoe_storeTheme {
        Welcome()
    }
}