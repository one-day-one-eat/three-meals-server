package team.sparta.onehouronemeal.domain.user.model.v1.subscription

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import team.sparta.onehouronemeal.domain.user.model.v1.User
import java.io.Serializable

@Entity
@Table(name = "subscription")
class Subscription(
    @EmbeddedId
    val id: SubscriptionId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subscribedUserId")
    @JoinColumn(name = "subscribed_user_id")
    val subscribedUser: User,
)

@Embeddable
data class SubscriptionId(
    val userId: Long,
    val subscribedUserId: Long
) : Serializable