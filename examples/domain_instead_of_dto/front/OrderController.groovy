package com.kakao.buy.front

class OrderController {
	private static final logger = LoggerFactory.getLogger(this)
	
	@Secured(["ROLE_BUYER"])
	def checkout() {
		ViewType viewType = ViewType.of(request)
		Channel channel = Channel.of(params['channel'] as Long, viewType)
		Cart cart = params.cartId ? Cart.get(params.cartId as Long) : null
		Long id = params['id'] as Long

		Payment payment = frontOrderService.findPaymentUsingJoin0(id)

		/**
		 * Checkout시에 Payment Status가 101이 아닐경우,
		 * 해당 Cart를 이용하여 Payment를 재생성 한다.
		 */
		if (!payment.isReadyToCheckout()) {
			GiveBackResult.SuccessfullyCreatedPayment.carry(null)
			redirect(url: payment.retryUrl)
		}

		Long accountId = buyerSecurityService.getUser()?.accountId
		BigDecimal kakaoPointBalance = billingRequestService.getKakaoPointBalance(accountId)
		List<ChannelPayMethod> channelPayMethods = ChannelPayMethod.findAllByChannelAndIsDisplay(channel, Boolean.TRUE).sort() {
			it.payMethod.ordering
		}

		withFormat {
			html {
				String layout = (channel.id == 3L ? 'main.farmer' : 'main.v2.header')
				switch (channel.id) {
					default:
						render(view: "/order/checkout", layout: layout, model: [
								channel           : channel, viewType: viewType,
								title             : '주문하기',
								payment           : payment,
								hasShippingAddress: shippingAddressService.hasUserAddress(accountId),
								addressList : shippingAddressService.findAddressOfUser(accountId, null),
								kakaoPointBalance: kakaoPointBalance,
								channelPayMethods: channelPayMethods,
								cart : cart
						])
						break
				}
			}
			json {
				render([

					channel: channel, viewType: viewType,
					title: '주문하기',
					payment: payment,
					hasShippingAddress: shippingAddressService.hasUserAddress(accountId),
					addressList : shippingAddressService.findAddressOfUser(accountId, null),
					kakaoPointBalance: kakaoPointBalance,
					channelPayMethods: channelPayMethods,
					cart : cart
				] as JSON)
			}
		}
	}
	
	@Secured(["ROLE_BUYER"])
	def list() {
		ViewType viewType = ViewType.of(request)
		Channel channel = Channel.of(params['channel'] as Long, viewType)
		Integer page = params['page'] as Integer ?: params['pageNo'] as Integer ?: 1
		Integer size = params['size'] as Integer ?: params['pageSize'] as Integer ?: 10

		int ITEMS_IN_A_PAGE = 10
		Long accountId = buyerSecurityService.getUser()?.accountId

		withFormat {
			html {
				switch (channel.id) {
					case 1L:
						String layout = 'main.gift.201505'
						Map counts = [
								gift: frontOrderService.countPaidPayments(accountId, Channel.GIFT),
								pick: frontOrderService.countPaidPayments(accountId, Channel.PICK),
						]
						render(view: "/order/1/list", layout: layout, model: [
								channel: channel, viewType: viewType,
								title  : '주문내역',
								page   : page, size: size,
								counts : counts,
						])
						break
					case 2L:
						String layout = 'main.pick.201505'
						Map counts = [
								gift: frontOrderService.countPaidPayments(accountId, Channel.GIFT),
								pick: frontOrderService.countPaidPayments(accountId, Channel.PICK),
						]
						render(view: "/order/2/list", layout: layout, model: [
								channel: channel, viewType: viewType,
								title  : '주문내역',
								page   : page, size: size,
								counts : counts,
						])
						break
					default:
						String layout = (channel.id == 3L ? 'main.farmer' : 'main.v2.header')
						Map counts = [
								this: frontOrderService.countPaidPayments(accountId, channel),
						]
						render(view: "/order/list", layout: layout, model: [
								channel: channel, viewType: viewType,
								title  : '주문내역',
								page   : page, size: size,
								counts : counts,
						])
				}
			}
			json {
				/* 주문내역을 가져오는데 지연시간이 길어 ajax 방식으로 구현하고, spinner를 보여준다. */
				PageSearchParam pageParam = new PageSearchParam(pageNo: page, pageSize: size)
				pageParam.pageSize = size ?: ITEMS_IN_A_PAGE
				pageParam.setPageInfo()

				PagedResult<Payment> result = frontOrderService.getPaymentListFast(pageParam, accountId, channel)
				checkAndRegisterMarshallers1()
				render result.toJson('/order/list')
			}
		}
	}
	
	@Secured(["ROLE_BUYER"])
	def show() {
		ViewType viewType = ViewType.of(request)
		Channel channel = Channel.of(params['channel'] as Long, viewType)
		Long id = params['id'] as Long

		Payment payment = frontOrderService.findPaymentUsingJoin0(id)
		payment = frontOrderService.addDeliveryInOrderDetail(payment)
		payment = frontOrderService.addButtonsInOrderDetail(payment)
		payment.fetchReceiverNickname()

		withFormat {
			html {
				switch (channel.id) {
					case 1L:
						String layout = 'main.gift.201505'
						render(view: "/order/1/show", layout: layout, model: [
								channel : channel, viewType: viewType,
								title   : '주문내역 상세보기',
								payment : payment,
								giveBack: flash.chainModel?.giveBack
						])
						break
					case 2L:
						String layout = 'main.gift.201505'
						render(view: "/order/2/show", layout: layout, model: [
								channel : channel, viewType: viewType,
								title   : '주문내역 상세보기',
								payment : payment,
								giveBack: flash.chainModel?.giveBack
						])
						break
					default:
						String layout = (channel.id == 3L ? 'main.farmer' : 'main.v2.header')
						render(view: "/order/show", layout: layout, model: [
								channel : channel, viewType: viewType,
								title   : '주문내역 상세보기',
								payment : payment,
								giveBack: flash.chainModel?.giveBack
						])
				}
			}
			json {
				render JsonUtil.toDeepJson([payment: payment, giveBack: flash.chainModel?.giveBack])
			}
		}
	}

}
