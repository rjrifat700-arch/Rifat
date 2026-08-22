package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProductItem
import com.example.ui.MainViewModel
import com.example.ui.components.FilterChipRow
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningContainer
import com.example.ui.theme.WarningOrange

@Composable
fun ProductsScreen(
    viewModel: MainViewModel,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedProductCategory.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()
    val cart by viewModel.cart.collectAsState()

    val categories = listOf(
        "সকল পণ্য",
        "কম্পিউটার এক্সেসরিজ",
        "স্টেশনারি ও খাতা",
        "ভ্যারাইটি ও গিফট"
    )

    val filteredProducts = products.filter { product ->
        val matchesCategory = (selectedCategory == "সকল পণ্য" || product.category == selectedCategory)
        val matchesSearch = if (searchQuery.isBlank()) true else {
            product.name.contains(searchQuery, ignoreCase = true) ||
            product.description.contains(searchQuery, ignoreCase = true) ||
            product.category.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    val totalCartItems = cart.values.sumOf { it.quantity }
    val totalCartAmount = cart.values.sumOf { it.product.price * it.quantity }

    Box(modifier = modifier.fillMaxSize().testTag("products_screen_content")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setProductSearchQuery(it) },
                placeholder = { Text("কম্পিউটার পার্টস, পেনড্রাইভ, খাতা ইত্যাদি খুঁজুন...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setProductSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("product_search_field")
            )

            // Categories Filter
            FilterChipRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setProductCategory(it) }
            )

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = if (totalCartItems > 0) 130.dp else 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredProducts.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "কোনো পণ্য পাওয়া যায়নি",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                items(filteredProducts) { product ->
                    val quantityInCart = cart[product.id]?.quantity ?: 0
                    ProductGridCard(
                        product = product,
                        quantityInCart = quantityInCart,
                        onAddToCart = { viewModel.addToCart(product) },
                        onRemoveFromCart = { viewModel.removeFromCart(product.id) }
                    )
                }
            }
        }

        // Bottom Sticky Floating Cart Bar
        AnimatedVisibility(
            visible = totalCartItems > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCart)
                    .testTag("sticky_cart_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "$totalCartItems টি আইটেম কার্টে আছে",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সর্বমোট: ৳$totalCartAmount",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("checkout_sticky_btn")
                    ) {
                        Text("অর্ডার করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: ProductItem,
    quantityInCart: Int,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().testTag("product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon & Category header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (product.iconType) {
                        "mouse" -> Icons.Filled.Mouse
                        "keyboard" -> Icons.Filled.Keyboard
                        "pendrive" -> Icons.Filled.Usb
                        "calculator" -> Icons.Filled.Calculate
                        "paper" -> Icons.Filled.MenuBook
                        "pen" -> Icons.Filled.Create
                        "cable" -> Icons.Filled.Cable
                        else -> Icons.Filled.Category
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )

                // Stock Badge Top Right
                Surface(
                    color = if (product.stockStatus.contains("ইন")) SuccessContainer else WarningContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = product.stockStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.stockStatus.contains("ইন")) SuccessGreen else WarningOrange,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                modifier = Modifier.height(34.dp)
            )

            Text(
                text = product.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Price & Quantity controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "৳${product.price}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.originalPrice > product.price) {
                        Text(
                            text = "৳${product.originalPrice}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                if (quantityInCart == 0) {
                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("add_cart_${product.id}")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("কার্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = onRemoveFromCart,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "কমান", modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "$quantityInCart",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "বাড়ান", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
